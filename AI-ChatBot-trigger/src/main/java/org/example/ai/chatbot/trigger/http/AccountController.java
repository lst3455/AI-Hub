package org.example.ai.chatbot.trigger.http;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.example.ai.chatbot.domain.account.model.entity.AdjustQuotaEntity;
import org.example.ai.chatbot.domain.account.model.valobj.AccountQuotaVO;
import org.example.ai.chatbot.domain.account.service.IAccountAdjustQuotaService;
import org.example.ai.chatbot.domain.account.service.IAccountQueryService;
import org.example.ai.chatbot.domain.auth.service.IAuthService;
import org.example.ai.chatbot.trigger.http.dto.AccountQuotaResponseDTO;
import org.example.ai.chatbot.trigger.http.dto.AdjustQuotaRequestDTO;
import org.example.ai.chatbot.trigger.http.dto.AdjustQuotaResponseDTO;
import org.example.ai.chatbot.types.common.Constants;
import org.example.ai.chatbot.types.exception.AiServiceException;
import org.example.ai.chatbot.types.model.Response;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.util.Map;

@Slf4j
@RestController
@CrossOrigin("${app.config.cross-origin}")
@RequestMapping("/api/${app.config.api-version}/account/")
public class AccountController {

    @Resource
    private Map<String, String> appTokenMap;

    @Resource
    private IAccountAdjustQuotaService accountAdjustQuotaService;

    @Resource
    private IAccountQueryService accountQueryService;

    @Resource
    private IAuthService authService;

    /**
     * Query the account quota for a given user.
     *
     * @param token  Authorization token from the request header
     * @param openId OpenId from the request header
     * @return Response containing the account quota information
     */
    @RequestMapping(value = "query_account_quota", method = RequestMethod.POST)
    public Response<AccountQuotaResponseDTO> queryAccountQuota(
            @RequestHeader("Authorization") String token,
            @RequestHeader("OpenId") String openId) {
        log.info("Query account quota, openId: {}", openId);
        try {
            // 1. Token validation
            boolean success = authService.checkToken(token);
            if (!success) {
                return Response.<AccountQuotaResponseDTO>builder()
                        .code(Constants.ResponseCode.TOKEN_ERROR.getCode())
                        .info(Constants.ResponseCode.TOKEN_ERROR.getInfo())
                        .build();
            }

            // 2. Parse token
            String openid = authService.openid(token);
            assert null != openid;

            // 3. Query quota
            AccountQuotaVO accountQuotaVO = accountQueryService.queryAccountQuota(openid);

            return Response.<AccountQuotaResponseDTO>builder()
                    .data(AccountQuotaResponseDTO.builder()
                            .totalQuota(accountQuotaVO.getTotalQuota())
                            .surplusQuota(accountQuotaVO.getSurplusQuota())
                            .build())
                    .code(Constants.ResponseCode.SUCCESS.getCode())
                    .info(Constants.ResponseCode.SUCCESS.getInfo())
                    .build();
        } catch (Exception e) {
            log.error("Query account quota failed, openId: {}", openId, e);
            return Response.<AccountQuotaResponseDTO>builder()
                    .code(Constants.ResponseCode.UN_ERROR.getCode())
                    .info(Constants.ResponseCode.UN_ERROR.getInfo())
                    .build();
        }
    }

    /**
     * Adjust the quota for a user account.
     *
     * Example usage with curl:
     * curl -X POST \
     *   http://localhost:8091/api/v1/account/adjust_quota \
     *   -H 'Content-Type: application/json;charset=utf-8' \
     *   -d '{
     *         "appId": "big-market",
     *         "appToken": "6ec604541f8b1ce4a",
     *         "openid": "xfg",
     *         "increaseQuota": 10
     *       }'
     *
     * @param adjustQuotaRequestDTO Request body containing appId, appToken, openid, and increaseQuota
     * @return Response containing the updated quota information
     */
    @RequestMapping(value = "adjust_quota", method = RequestMethod.POST)
    public Response<AdjustQuotaResponseDTO> adjustQuota(@RequestBody AdjustQuotaRequestDTO adjustQuotaRequestDTO) {
        log.info("Account quota adjustment started: openid={}, increaseQuota={}",
                adjustQuotaRequestDTO.getOpenid(), adjustQuotaRequestDTO.getIncreaseQuota());
        try {
            // 0. Parameter validation
            if (StringUtils.isBlank(adjustQuotaRequestDTO.getOpenid()) ||
                    StringUtils.isBlank(adjustQuotaRequestDTO.getAppId()) ||
                    StringUtils.isBlank(adjustQuotaRequestDTO.getAppToken())) {
                throw new AiServiceException(Constants.ResponseCode.ILLEGAL_PARAMETER.getCode(),
                        Constants.ResponseCode.ILLEGAL_PARAMETER.getInfo());
            }

            // 1. appId and appToken validation
            if (!adjustQuotaRequestDTO.getAppToken().equals(appTokenMap.get(adjustQuotaRequestDTO.getAppId()))) {
                throw new AiServiceException(Constants.ResponseCode.APP_TOKEN_ERROR.getCode(),
                        Constants.ResponseCode.APP_TOKEN_ERROR.getInfo());
            }

            // 2. Adjust account quota
            AccountQuotaVO accountQuotaVO = accountAdjustQuotaService.adjustQuota(
                    AdjustQuotaEntity.builder()
                            .openid(adjustQuotaRequestDTO.getOpenid())
                            .increaseQuota(adjustQuotaRequestDTO.getIncreaseQuota())
                            .build());

            log.info("Account quota adjustment completed: openid={}, totalQuota={}",
                    adjustQuotaRequestDTO.getOpenid(), accountQuotaVO.getTotalQuota());

            // 3. Build response
            return Response.<AdjustQuotaResponseDTO>builder()
                    .code(Constants.ResponseCode.SUCCESS.getCode())
                    .info(Constants.ResponseCode.SUCCESS.getInfo())
                    .data(AdjustQuotaResponseDTO.builder()
                            .totalQuota(accountQuotaVO.getTotalQuota())
                            .surplusQuota(accountQuotaVO.getSurplusQuota())
                            .build())
                    .build();
        } catch (Exception e) {
            log.error("Account quota adjustment failed: openid={}, increaseQuota={}",
                    adjustQuotaRequestDTO.getOpenid(), adjustQuotaRequestDTO.getIncreaseQuota(), e);
            return Response.<AdjustQuotaResponseDTO>builder()
                    .code(Constants.ResponseCode.UN_ERROR.getCode())
                    .info(Constants.ResponseCode.UN_ERROR.getInfo())
                    .build();
        }
    }
}