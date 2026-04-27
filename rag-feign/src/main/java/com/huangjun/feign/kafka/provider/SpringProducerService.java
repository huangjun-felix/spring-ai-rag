package com.huangjun.feign.kafka.provider;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

@Service
public class SpringProducerService {

    // 直接注入，无需实例化配置
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    public void sendMessage(String topic, String key, String value) {
        Message<String> message = MessageBuilder.withPayload(value)
                .setHeader(KafkaHeaders.TOPIC, topic)
                .setHeader(KafkaHeaders.KEY, key)
                .setHeader("session_id", 123)
                .build();
        // 直接发送，Spring 处理了底层细节
        kafkaTemplate.send(message)
                     .whenComplete((result, ex) -> {
                         if (ex == null) {
                             System.out.println("Spring 发送成功：" + result.getRecordMetadata().offset());
                         }
                     });
    }
}