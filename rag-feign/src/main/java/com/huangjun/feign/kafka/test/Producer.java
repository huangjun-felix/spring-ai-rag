package com.huangjun.feign.kafka.test;

import jakarta.annotation.Nonnull;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Future;

public class Producer {

    public static void main(String[] args) {

        KafkaProducer<String, String> kafkaProducer = getKafkaProducer();
        ProducerRecord<String, String> producerRecord;
        for (int i = 0; i < 1000; i++) {
            producerRecord = new ProducerRecord<>("kafka-cluster", "send to consumer"+i);
            Future<RecordMetadata> send = kafkaProducer.send(producerRecord);
        }
        kafkaProducer.close();
    }

    @Nonnull
    public static KafkaProducer<String,String> getKafkaProducer(){
        List<String> hosts = Arrays.asList("192.168.1.137:9094","192.168.1.137:9095","192.168.1.137:9096");
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,hosts);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        return new KafkaProducer<>(props);
    }

}
