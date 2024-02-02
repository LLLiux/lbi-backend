package com.lin.lbi.mq;

import com.lin.lbi.constant.MQConstant;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class BIMQInitMain {
    public static void main(String[] args) {
        ConnectionFactory factory = new ConnectionFactory();
        try {
            Connection connection = factory.newConnection();
            Channel channel = connection.createChannel();
            channel.exchangeDeclare(MQConstant.BI_EXCHANGE_NAME, BuiltinExchangeType.DIRECT);
            channel.queueDeclare(MQConstant.BI_QUEUE_NAME, true, false, false, null);
            channel.queueBind(MQConstant.BI_QUEUE_NAME, MQConstant.BI_EXCHANGE_NAME, MQConstant.BI_ROUTING_KEY);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
