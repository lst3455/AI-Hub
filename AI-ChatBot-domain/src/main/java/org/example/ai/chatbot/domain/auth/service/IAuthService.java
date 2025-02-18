package org.example.ai.chatbot.domain.auth.service;


import org.example.ai.chatbot.domain.auth.model.entity.AuthStateEntity;


public interface IAuthService {

    /**
     * 登录验证
     * @param code 验证码
     * @return Token
     */
    AuthStateEntity doLogin(String code, String openId);

    boolean checkToken(String token);

    String openid(String token);
}
