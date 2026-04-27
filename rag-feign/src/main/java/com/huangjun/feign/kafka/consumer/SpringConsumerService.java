package com.huangjun.feign.kafka.consumer;

import io.swagger.v3.oas.annotations.headers.Header;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.stereotype.Component;

@Component
public class SpringConsumerService {

    // 只要加上这个注解，Spring 就会自动把拉取到的消息传给这个方法
    @KafkaListener(topics = "kafka-cluster", groupId = "spring-boot-group")
    public void listen(ConsumerRecord<String, String> record) {
        System.out.printf("Spring 注解收到消息: 分区 = %d, offset = %d, value = %s%n", 
                record.partition(), record.offset(), record.value());
    }
    
    // 如果你只关心消息体本身，甚至可以不用 ConsumerRecord，直接接收 String
    @KafkaListener(topics = "kafka-cluster", groupId = "spring-boot-group-2")
    public void listenSimple(String message) {
        System.out.println("业务消息体直接处理: " + message);
    }
}