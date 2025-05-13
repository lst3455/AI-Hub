package org.example.ai.chatbot.domain.rebate.service;


import org.example.ai.chatbot.domain.rebate.port.IRebatePort;

import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;



@Service
public class RebateService implements IRebateService {

    @Resource
    private IRebatePort port;

    @Override
    public void rebateGoods(String openid, String orderId) {
        port.rebate(openid, orderId);
    }

}
