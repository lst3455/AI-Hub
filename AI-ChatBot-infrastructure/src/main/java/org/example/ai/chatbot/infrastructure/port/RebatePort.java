package org.example.ai.chatbot.infrastructure.port;


import org.example.ai.chatbot.domain.rebate.port.IRebatePort;
import org.example.ai.chatbot.infrastructure.gateway.RebateServiceRPC;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author Fuzhengwei bugstack.cn @小傅哥
 * @description 适配端口
 * @create 2024-10-20 14:51
 */
@Service
public class RebatePort implements IRebatePort {

    @Resource
    private RebateServiceRPC rebateServiceRPC;

    @Override
    public void rebate(String userId, String orderId) {
        rebateServiceRPC.rebate(userId, orderId);
    }

}
