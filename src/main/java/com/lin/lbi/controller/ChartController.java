package com.lin.lbi.controller;

import cn.hutool.core.io.FileUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.gson.Gson;
import com.lin.lbi.annotation.AuthCheck;
import com.lin.lbi.common.BaseResponse;
import com.lin.lbi.common.DeleteRequest;
import com.lin.lbi.common.ErrorCode;
import com.lin.lbi.common.ResultUtils;
import com.lin.lbi.constant.UserConstant;
import com.lin.lbi.exception.BusinessException;
import com.lin.lbi.exception.ThrowUtils;
import com.lin.lbi.manager.AIManager;
import com.lin.lbi.manager.RedissonManager;
import com.lin.lbi.model.dto.chart.*;
import com.lin.lbi.model.entity.Chart;
import com.lin.lbi.model.entity.User;
import com.lin.lbi.model.enums.AIGenerateStatusEnum;
import com.lin.lbi.model.vo.AIResponse;
import com.lin.lbi.service.ChartService;
import com.lin.lbi.service.UserService;
import com.lin.lbi.utils.ExcelUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 帖子接口
 *
 * @author L
 */
@RestController
@RequestMapping("/chart")
@Slf4j
public class ChartController {

    @Resource
    private ChartService chartService;

    @Resource
    private UserService userService;

    @Resource
    private AIManager aiManager;

    @Resource
    private RedissonManager redissonManager;

    @Resource
    private ThreadPoolExecutor threadPoolExecutor;

    private static final Long FILE_SIZE_LIMIT = 1024 * 1024L;

    private static final List<String> VALID_FILE_SUFFIX_LIST = Arrays.asList("xlsx", "xls");

    private final static Gson GSON = new Gson();

    /**
     * AI 生成（线程池异步）
     *
     * @param multipartFile
     * @param uploadChartRequest
     * @param request
     * @return
     */
    @PostMapping("/generateByAI")
    public BaseResponse<AIResponse> generateByAI(@RequestPart("file") MultipartFile multipartFile,
                                                      UploadChartRequest uploadChartRequest, HttpServletRequest request) {
        // 校验
        String goal = uploadChartRequest.getGoal();
        ThrowUtils.throwIf(StringUtils.isBlank(goal), ErrorCode.PARAMS_ERROR, "分析目标为空");
        ThrowUtils.throwIf(goal.length() > 1000, ErrorCode.PARAMS_ERROR, "分析目标过长");
        String chartName = uploadChartRequest.getChartName();
        ThrowUtils.throwIf(StringUtils.isNotBlank(chartName) && chartName.length() > 100, ErrorCode.PARAMS_ERROR, "图表名称过长");
        ThrowUtils.throwIf(multipartFile.getSize() > FILE_SIZE_LIMIT, ErrorCode.PARAMS_ERROR, "上传文件过大");
        String fileSuffix = FileUtil.getSuffix(multipartFile.getOriginalFilename());
        ThrowUtils.throwIf(!VALID_FILE_SUFFIX_LIST.contains(fileSuffix), ErrorCode.PARAMS_ERROR, "上传文件不符合要求");
        User loginUser = userService.getLoginUser(request);
        Long userId = loginUser.getId();
        // 限流
        String rateLimitKey = "generateByAI_" + userId;
        boolean canOp = redissonManager.doRateLimit(rateLimitKey);
        ThrowUtils.throwIf(!canOp, ErrorCode.TOO_MANY_REQUEST);
        // 处理上传文件
        String data = ExcelUtils.excelToCsv(multipartFile);
        // 添加数据库
        String chartType = uploadChartRequest.getChartType();
        Chart saveChart = new Chart();
        saveChart.setData(data);
        saveChart.setGoal(goal);
        saveChart.setChartName(chartName);
        saveChart.setChartType(chartType);
        saveChart.setStatus(AIGenerateStatusEnum.WAITING.getValue());
        saveChart.setUserId(userId);
        boolean save = chartService.save(saveChart);
        ThrowUtils.throwIf(!save, ErrorCode.SYSTEM_ERROR, "图表信息保存失败");
        Long chartId = saveChart.getId();
        // 拼接输入
        StringBuilder input = new StringBuilder();
        input.append("分析需求：").append("\n").append(goal);
        input.append("原始数据：").append("\n").append(data);
        if (StringUtils.isNotBlank(chartType)) {
            input.append("图表类型：").append("\n").append(chartType);
        }
        // 异步执行
        CompletableFuture.runAsync(() -> {
            Chart updateChart = new Chart();
            updateChart.setId(chartId);
            updateChart.setStatus(AIGenerateStatusEnum.RUNNING.getValue());
            boolean update = chartService.updateById(updateChart);
            if (!update) {
                handleError(chartId, "执行状态(执行中)更新失败");
                return;
            }
            // 调用 AI
            String output = aiManager.doChat(input.toString());
            String[] split = output.split("【【【【【");
            if (split.length < 3) {
                handleError(chartId, "AI 生成失败");
                return;
            }
            // 获取结果并更新数据库
            Chart result = new Chart();
            result.setId(chartId);
            result.setStatus(AIGenerateStatusEnum.SUCCEED.getValue());
            result.setGenChart(split[1]);
            result.setGenResult(split[2]);
            boolean updateResult = chartService.updateById(result);
            if (!updateResult) {
                handleError(chartId, "执行状态(成功)更新失败");
            }
        }, threadPoolExecutor);

        // 返回 VO
        AIResponse aiResponse = new AIResponse();
        aiResponse.setId(chartId);
//        aiResponse.setGenChart(split[1]);
//        aiResponse.setGenResult(split[2]);

        return ResultUtils.success(aiResponse);
    }

    private void handleError(Long chartId, String message) {
        Chart chart = new Chart();
        chart.setId(chartId);
        chart.setStatus(AIGenerateStatusEnum.FAILED.getValue());
        chart.setExecMessage(message);
        boolean update = chartService.updateById(chart);
        ThrowUtils.throwIf(!update, ErrorCode.SYSTEM_ERROR, "数据库更新失败");
    }

    // region 增删改查

    /**
     * 创建
     *
     * @param chartAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Long> addChart(@RequestBody ChartAddRequest chartAddRequest, HttpServletRequest request) {
        if (chartAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Chart chart = new Chart();
        BeanUtils.copyProperties(chartAddRequest, chart);

        User loginUser = userService.getLoginUser(request);
        chart.setUserId(loginUser.getId());

        boolean result = chartService.save(chart);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        long newChartId = chart.getId();
        return ResultUtils.success(newChartId);
    }

    /**
     * 删除
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteChart(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        // 判断是否存在
        Chart oldChart = chartService.getById(id);
        ThrowUtils.throwIf(oldChart == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可删除
        if (!oldChart.getUserId().equals(user.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean b = chartService.removeById(id);
        return ResultUtils.success(b);
    }

    /**
     * 更新（仅管理员）
     *
     * @param chartUpdateRequest
     * @return
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateChart(@RequestBody ChartUpdateRequest chartUpdateRequest) {
        if (chartUpdateRequest == null || chartUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Chart chart = new Chart();
        BeanUtils.copyProperties(chartUpdateRequest, chart);

        long id = chartUpdateRequest.getId();
        // 判断是否存在
        Chart oldChart = chartService.getById(id);
        ThrowUtils.throwIf(oldChart == null, ErrorCode.NOT_FOUND_ERROR);
        boolean result = chartService.updateById(chart);
        return ResultUtils.success(result);
    }

    /**
     * 根据 id 获取
     *
     * @param id
     * @return
     */
    @GetMapping("/get/vo")
    public BaseResponse<Chart> getChartById(long id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Chart chart = chartService.getById(id);
        if (chart == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        return ResultUtils.success(chart);
    }

    /**
     * 分页获取列表（封装类）
     *
     * @param chartQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<Chart>> listChartByPage(@RequestBody ChartQueryRequest chartQueryRequest,
                                                     HttpServletRequest request) {
        long current = chartQueryRequest.getCurrent();
        long size = chartQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<Chart> chartPage = chartService.page(new Page<>(current, size),
                chartService.getQueryWrapper(chartQueryRequest));
        return ResultUtils.success(chartPage);
    }

    /**
     * 分页获取当前用户创建的资源列表
     *
     * @param chartQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/my/list/page/vo")
    public BaseResponse<Page<Chart>> listMyChartByPage(@RequestBody ChartQueryRequest chartQueryRequest,
                                                       HttpServletRequest request) {
        if (chartQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        chartQueryRequest.setUserId(loginUser.getId());
        long current = chartQueryRequest.getCurrent();
        long size = chartQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<Chart> chartPage = chartService.page(new Page<>(current, size),
                chartService.getQueryWrapper(chartQueryRequest));
        return ResultUtils.success(chartPage);
    }

    // endregion

    /**
     * 编辑（用户）
     *
     * @param chartEditRequest
     * @param request
     * @return
     */
    @PostMapping("/edit")
    public BaseResponse<Boolean> editChart(@RequestBody ChartEditRequest chartEditRequest, HttpServletRequest request) {
        if (chartEditRequest == null || chartEditRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Chart chart = new Chart();
        BeanUtils.copyProperties(chartEditRequest, chart);

        User loginUser = userService.getLoginUser(request);
        long id = chartEditRequest.getId();
        // 判断是否存在
        Chart oldChart = chartService.getById(id);
        ThrowUtils.throwIf(oldChart == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可编辑
        if (!oldChart.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean result = chartService.updateById(chart);
        return ResultUtils.success(result);
    }

}
