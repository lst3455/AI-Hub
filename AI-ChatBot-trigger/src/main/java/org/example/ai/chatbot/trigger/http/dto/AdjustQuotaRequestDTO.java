package org.example.ai.chatbot.trigger.http.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AdjustQuotaRequestDTO {

    /** 请求应用ID */
    private String appId;
    /** 请求应用Token */
    private String appToken;

    /** 用户ID；微信分配的唯一ID编码 */
    private String openid;
    /** 调增额度 */
    private Integer increaseQuota;

}
