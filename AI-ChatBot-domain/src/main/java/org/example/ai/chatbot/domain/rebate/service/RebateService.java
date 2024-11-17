package org.example.ai.chatbot.domain.rebate.service;


import org.example.ai.chatbot.domain.rebate.port.IRebatePort;

import org.springframework.stereotype.Service;

import javax.annotation.Resource;


/**
 * @author Fuzhengwei bugstack.cn @小傅哥
 * @description 订单服务
 * @create 2023-10-05 13:10
 */
@Service
public class RebateService implements IRebateService {

    @Resource
    private IRebatePort port;

    @Override
    public void rebateGoods(String openid, String orderId) {
        port.rebate(openid, orderId);
    }

}
