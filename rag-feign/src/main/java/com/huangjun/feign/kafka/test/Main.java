package com.huangjun.feign.kafka.test;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

public class Main {

    public static class Consumer{

        public static void main(String[] args){
            KafkaConsumer<String, String> kafkaConsumer = getStringStringKafkaConsumer();
            kafkaConsumer.subscribe(Collections.singletonList("kafka-cluster"));
            System.out.println("准备加入消费组----------------------");
            try{
                while (true){
                    ConsumerRecords<String, String> records = kafkaConsumer.poll(Duration.ofMillis(100));
                    records.forEach(record -> {
                        System.out.println(record.partition()+":"+record.value());
                    });
                }
            }finally {
                kafkaConsumer.close();
            }
        }

        @NotNull
        private static KafkaConsumer<String, String> getStringStringKafkaConsumer() {
            List<String> hosts = Arrays.asList("192.168.1.137:9094","192.168.1.137:9095","192.168.1.137:9096");
            Properties props = new Properties();
            props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,hosts);
            props.put(ConsumerConfig.GROUP_ID_CONFIG,"test");
            props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
            props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");

            KafkaConsumer<String, String> kafkaConsumer = new KafkaConsumer<>(props);
            return kafkaConsumer;
        }
    }

}
