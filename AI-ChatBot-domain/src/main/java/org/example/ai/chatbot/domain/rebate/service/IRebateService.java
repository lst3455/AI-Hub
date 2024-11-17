package org.example.ai.chatbot.domain.rebate.service;


/**
 * @author Fuzhengwei bugstack.cn @小傅哥
 * @description 订单服务
 * 1. 用户下单 createOrder
 * @create 2023-10-05 10:49
 */
public interface IRebateService {


    void rebateGoods(String openid, String orderId);

}