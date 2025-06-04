package org.example.ai.chatbot.trigger.http;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.cache.Cache;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.example.ai.chatbot.domain.auth.model.entity.AuthStateEntity;
import org.example.ai.chatbot.domain.auth.model.valobj.AuthTypeVO;
import org.example.ai.chatbot.domain.auth.service.IAuthService;
import org.example.ai.chatbot.types.common.Constants;
import org.example.ai.chatbot.types.model.Response;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;

/**
 * This controller manages authentication operations, including user login via verification code
 * and generating verification codes for authentication.
 *
 * Endpoint base URL: /api/${app.config.api-version}/auth/
 */
@Slf4j
@RestController()
@CrossOrigin("${app.config.cross-origin}")
@RequestMapping("/api/${app.config.api-version}/auth/")
public class AuthController {

    @Resource
    private IAuthService authService;

    @Resource
    private Cache<String, String> codeCache;

    /**
     * Authenticates a user based on a provided verification code.
     * If authentication is successful, a token is returned; otherwise, an error code is returned.
     *
     * Usage:
     * curl -X POST http://localhost:8090/api/v0/auth/login -H 'Content-Type: application/x-www-form-urlencoded' -d 'code=xxxx'
     *
     * Endpoint: POST /auth/login
     *
     * @param code The verification code provided by the user for authentication.
     * @return A Response object containing the authentication result, including a token if successful.
     */
    @RequestMapping(value = "login", method = RequestMethod.POST)
    public Response<JSONObject> doLogin(@RequestParam("code") String code, @RequestParam("openId") String openId) {
        log.info("Authentication login check started, verification code: {}, openId:{}", code, openId);
        try {
            // Attempt to authenticate using the provided code
            AuthStateEntity authStateEntity = authService.doLogin(code, openId);
            log.info("Authentication login check completed, verification code: {} Result: {}", code, JSON.toJSONString(authStateEntity));

            // Intercept if authentication failed
            if (!AuthTypeVO.A0000.getCode().equals(authStateEntity.getCode())) {
                return Response.<JSONObject>builder()
                        .code(Constants.ResponseCode.TOKEN_ERROR.getCode())
                        .info(Constants.ResponseCode.TOKEN_ERROR.getInfo())
                        .build();
            }

            JSONObject jsonData = new JSONObject();
            jsonData.put("token", authStateEntity.getToken());
            jsonData.put("expireDate", authStateEntity.getExpireDate());

            // Allow if authentication succeeded
            return Response.<JSONObject>builder()
                    .code(Constants.ResponseCode.SUCCESS.getCode())
                    .info(Constants.ResponseCode.SUCCESS.getInfo())
                    .data(jsonData)
                    .build();

        } catch (Exception e) {
            log.error("Authentication login check failed, verification code: {}", code);
            return Response.<JSONObject>builder()
                    .code(Constants.ResponseCode.UN_ERROR.getCode())
                    .info(Constants.ResponseCode.UN_ERROR.getInfo())
                    .build();
        }
    }

    /**
     * Generates a new verification code associated with a provided user ID (openid).
     * The generated code is stored in the cache and linked to the openid for future verification.
     *
     * Usage:
     * http://localhost:8090/api/v0/auth/code_create?openid=123456
     *
     * Endpoint: GET /auth/code_create
     *
     * @param openid The unique identifier for the user (e.g., WeChat openid) requesting a verification code.
     * @return A Response object containing the generated verification code or an error if unsuccessful.
     */
    @RequestMapping(value = "code_create", method = RequestMethod.POST)
    public Response<String> codeCreate(@RequestParam("openid") String openid) {
        log.info("Verification code creation started, openid: {}", openid);
        try {
            // Generate a 4-digit random numeric code
            String code = RandomStringUtils.randomNumeric(4);
            codeCache.put(code, openid);
            codeCache.put(openid, code);
            log.info("Verification code created, code: {}", code);

            // Return the generated code if successful
            return Response.<String>builder()
                    .code(Constants.ResponseCode.SUCCESS.getCode())
                    .info(Constants.ResponseCode.SUCCESS.getInfo())
                    .data(code)
                    .build();

        } catch (Exception e) {
            log.error("Verification code creation failed, openid: {}", openid);
            return Response.<String>builder()
                    .code(Constants.ResponseCode.UN_ERROR.getCode())
                    .info(Constants.ResponseCode.UN_ERROR.getInfo())
                    .build();
        }
    }
}
