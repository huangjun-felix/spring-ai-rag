package com.huangjun.consumer.listsenner;

import com.huangjun.common.contants.KafkaConstants;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.jetbrains.annotations.NotNull;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

@Component
public class TestDemoConsumer {

    @KafkaListener(topics = KafkaConstants.KAFKA_DEMO_TOPIC,groupId = "test-demo")
    public void getMessage(@NotNull ConsumerRecord<String, String> record, Acknowledgment ack) {
        System.out.println("message  :"+record.value());
        ack.acknowledge();
        AtomicInteger atomicInteger = new AtomicInteger(1);
        int i = atomicInteger.incrementAndGet();
    }

}
