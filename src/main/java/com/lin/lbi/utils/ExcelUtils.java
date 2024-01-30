package com.lin.lbi.utils;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.lin.lbi.common.ErrorCode;
import com.lin.lbi.exception.ThrowUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class ExcelUtils {
    public static String excelToCsv(MultipartFile multipartFile) {
//        File file = null;
//        try {
//            file = ResourceUtils.getFile("D:\\IDEA_project\\lbi-backend\\src\\main\\resources\\test_data.xlsx");
//        } catch (FileNotFoundException e) {
//            throw new RuntimeException(e);
//        }

        List<Map<Integer, String>> list = null;
        try {
            list = EasyExcel.read(multipartFile.getInputStream())
                    .excelType(ExcelTypeEnum.XLSX)
                    .sheet()
                    .headRowNumber(0)
                    .doReadSync();
        } catch (IOException e) {
            log.error("表格读取失败", e);
        }

        // 检查传入文件是否有效
        ThrowUtils.throwIf(!valid(list), ErrorCode.PARAMS_ERROR, "上传文件无效");

        StringBuilder result = new StringBuilder();
        // 拼接表头
        LinkedHashMap<Integer, String> headerMap = (LinkedHashMap) list.get(0);
        List<String> headerList = headerMap.values().stream().filter(StringUtils::isNotBlank).collect(Collectors.toList());
        result.append(StringUtils.join(headerList, ",")).append("\n");
        // 拼接数据
        for (int i = 1; i < list.size(); i++) {
            LinkedHashMap<Integer, String> dataMap = (LinkedHashMap) list.get(i);
            List<String> dataList = dataMap.values().stream().filter(StringUtils::isNotBlank).collect(Collectors.toList());
            result.append(StringUtils.join(dataList, ",")).append("\n");
        }

        return result.toString();
    }

    private static boolean valid(List<Map<Integer, String>> list) {
        return !(CollUtil.isEmpty(list) || list.size() <= 1);
    }
}
