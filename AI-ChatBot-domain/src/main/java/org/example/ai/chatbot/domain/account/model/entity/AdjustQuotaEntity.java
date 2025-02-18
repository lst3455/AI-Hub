package org.example.ai.chatbot.domain.account.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AdjustQuotaEntity {

    /** 用户ID；微信分配的唯一ID编码 */
    private String openid;
    /** 调增额度 */
    private Integer increaseQuota;

}
