package com.lin.lbi.mq;

import com.lin.lbi.common.ErrorCode;
import com.lin.lbi.constant.MQConstant;
import com.lin.lbi.exception.ThrowUtils;
import com.lin.lbi.manager.AIManager;
import com.lin.lbi.model.entity.Chart;
import com.lin.lbi.model.enums.AIGenerateStatusEnum;
import com.lin.lbi.service.ChartService;
import com.lin.lbi.service.UserService;
import com.rabbitmq.client.Channel;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Date;

@Component
@Slf4j
public class BIMessageConsumer {
    @Resource
    private ChartService chartService;
    @Resource
    private AIManager aiManager;

    @SneakyThrows
    @RabbitListener(queues = {MQConstant.BI_QUEUE_NAME}, ackMode = "MANUAL")
    public void receiveMsg(String message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        Long chartId = Long.parseLong(message);
        // 更新状态为 执行中
        Chart updateChart = new Chart();
        updateChart.setId(chartId);
        updateChart.setStatus(AIGenerateStatusEnum.RUNNING.getValue());
        boolean update = chartService.updateById(updateChart);
        if (!update) {
            channel.basicNack(deliveryTag, false, false);
            handleError(chartId, "执行状态(执行中)更新失败");
            return;
        }
        // 拼接输入
        String input = buildInput(chartId);
        // 调用 AI
        String output = aiManager.doChat(input);
        String[] split = output.split("【【【【【");
        if (split.length < 3) {
            channel.basicNack(deliveryTag, false, false);
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
            channel.basicNack(deliveryTag, false, false);
            handleError(chartId, "执行状态(成功)更新失败");
        }
        channel.basicAck(deliveryTag, false);
    }

    @NotNull
    private String buildInput(Long chartId) {
        Chart chart = chartService.getById(chartId);
        String data = chart.getData();
        String goal = chart.getGoal();
        String chartType = chart.getChartType();

        StringBuilder input = new StringBuilder();
        input.append("分析需求：").append("\n").append(goal);
        input.append("原始数据：").append("\n").append(data);
        if (StringUtils.isNotBlank(chartType)) {
            input.append("图表类型：").append("\n").append(chartType);
        }
        return input.toString();
    }

    private void handleError(Long chartId, String message) {
        Chart chart = new Chart();
        chart.setId(chartId);
        chart.setStatus(AIGenerateStatusEnum.FAILED.getValue());
        chart.setExecMessage(message);
        boolean update = chartService.updateById(chart);
        ThrowUtils.throwIf(!update, ErrorCode.SYSTEM_ERROR, "数据库更新失败");
    }
}
