package org.example.ai.chatbot.infrastructure.gateway;

import com.alibaba.fastjson.JSON;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.example.trigger.api.IRebateService;
import org.example.trigger.api.dto.RebateRequestDTO;
import org.example.trigger.api.request.Request;
import org.example.trigger.api.response.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class RebateServiceRPC {

    @DubboReference(interfaceClass = IRebateService.class, version = "1.0", check = true, timeout = 5000, retries = 3)
    private IRebateService rebateService;

    @Value("${app.config.big-market.appId}")
    private String appId;
    @Value("${app.config.big-market.appToken}")
    private String appToken;

    @PostConstruct
    public void validateRebateService() {
        if (rebateService == null) {
            log.warn("RebateService is not injected. Please check the Dubbo configuration.");
        }
    }

    public boolean rebate(String userId, String orderId) {
        try {
            if (rebateService == null) {
                log.error("RebateService reference is null for userId:{} orderId:{}", userId, orderId);
                return false;
            }

            RebateRequestDTO requestDTO = new RebateRequestDTO();
            requestDTO.setUserId(userId);
            requestDTO.setOutBusinessNo(orderId);
            requestDTO.setBehaviorType("CHATBOT");

            Request<RebateRequestDTO> request = new Request<>();
            request.setAppId(appId);
            request.setAppToken(appToken);
            request.setData(requestDTO);

            Response<Boolean> response = rebateService.rebate(request);
            log.info("支付返利操作开始，request:{} response:{}", JSON.toJSONString(request), JSON.toJSONString(response));
            return "0000".equals(response.getCode());
        } catch (Exception e) {
            log.error("支付返利操作失败，userId:{} orderId:{}", userId, orderId, e);
            return false;
        }
    }
}