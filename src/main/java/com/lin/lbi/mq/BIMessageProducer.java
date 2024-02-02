package com.lin.lbi.mq;

import com.lin.lbi.constant.MQConstant;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class BIMessageProducer {

    @Resource
    private RabbitTemplate rabbitTemplate;

    public void sendMsg(String message) {
        rabbitTemplate.convertAndSend(MQConstant.BI_EXCHANGE_NAME, MQConstant.BI_ROUTING_KEY, message);
    }
}
