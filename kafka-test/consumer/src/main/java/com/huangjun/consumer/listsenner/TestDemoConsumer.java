package com.huangjun.consumer.listsenner;

import com.huangjun.common.contants.KafkaConstants;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.jetbrains.annotations.NotNull;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class TestDemoConsumer {

    @KafkaListener(topics = KafkaConstants.KAFKA_DEMO_TOPIC,groupId = "test-demo")
    public void getMessage(@NotNull ConsumerRecord<String, String> record){
        System.out.println("message  :"+record.value());
    }

}
