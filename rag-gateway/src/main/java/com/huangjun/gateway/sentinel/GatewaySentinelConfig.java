package com.huangjun.gateway.sentinel;

import com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayFlowRule;
import com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayRuleManager;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;

import java.util.HashSet;
import java.util.Set;

@Configuration
public class GatewaySentinelConfig {

    @PostConstruct
    public void init() {
        Set<GatewayFlowRule> rules = new HashSet<>();
        rules.add(new GatewayFlowRule("file_route")
                .setCount(10).setIntervalSec(1));
        GatewayRuleManager.loadRules(rules);
    }

}
