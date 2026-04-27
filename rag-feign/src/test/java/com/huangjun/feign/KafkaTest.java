package com.huangjun.feign;

import com.huangjun.feign.kafka.provider.SpringProducerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class KafkaTest {


    @Autowired
    private SpringProducerService springProducerService;

    @Test
    public void test() {
        springProducerService.sendMessage("kafka-cluster","key_1","nihao");
    }

}
