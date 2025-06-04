package org.example.ai.chatbot.infrastructure.port;


import org.example.ai.chatbot.domain.rebate.port.IRebatePort;
import org.example.ai.chatbot.infrastructure.gateway.RebateServiceRPC;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;


@Service
public class RebatePort implements IRebatePort {

    @Resource
    private RebateServiceRPC rebateServiceRPC;

    @Override
    public void rebate(String userId, String orderId) {
        rebateServiceRPC.rebate(userId, orderId);
    }

}
