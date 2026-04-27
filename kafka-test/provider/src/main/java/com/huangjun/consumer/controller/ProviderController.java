package com.huangjun.consumer.controller;

import com.huangjun.common.contants.KafkaConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/kafka")
public class ProviderController {

    private KafkaTemplate<String,String> kafkaTemplate;
    @Autowired
    public void init(KafkaTemplate<String,String> kafkaTemplate){
        this.kafkaTemplate = kafkaTemplate;
    }

    @PostMapping("/test/{message}")
    public void testKafkaDemo(@PathVariable("message") String message){
        kafkaTemplate.send(KafkaConstants.KAFKA_DEMO_TOPIC,message);
    }

}
