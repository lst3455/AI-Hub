package org.example.ai.chatbot.domain.auth.service;

import com.google.common.cache.Cache;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.example.ai.chatbot.domain.auth.model.entity.AuthStateEntity;
import org.example.ai.chatbot.domain.auth.model.valobj.AuthTypeVO;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;

/**
 * @description Authentication service
 * @create 2023-08-05 18:23
 */
@Slf4j
@Service
public class AuthService extends AbstractAuthService {

    @Resource
    private Cache<String, String> codeCache;

    @Override
    protected AuthStateEntity checkCode(String code) {
        // Validate the verification code
        String openId = codeCache.getIfPresent(code);
        if (StringUtils.isBlank(openId)){
            log.info("Authentication failed, the verification code entered by the user does not exist: {}", code);
            return AuthStateEntity.builder()
                    .code(AuthTypeVO.A0001.getCode())
                    .info(AuthTypeVO.A0001.getInfo())
                    .build();
        }

        // Invalidate the cached keys
        codeCache.invalidate(openId);
        codeCache.invalidate(code);

        // Verification code is valid
        return AuthStateEntity.builder()
                .code(AuthTypeVO.A0000.getCode())
                .info(AuthTypeVO.A0000.getInfo())
                .openId(openId)
                .build();
    }

    @Override
    public boolean checkToken(String token) {
        return isVerify(token);
    }

    @Override
    public String openid(String token) {
        Claims claims = decode(token);
        return claims.get("openId").toString();
    }
}
