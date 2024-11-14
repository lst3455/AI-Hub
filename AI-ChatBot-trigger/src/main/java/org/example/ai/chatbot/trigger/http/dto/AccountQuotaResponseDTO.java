package org.example.ai.chatbot.trigger.http.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Fuzhengwei bugstack.cn @小傅哥
 * @description 账户额度传输对象
 * @create 2024-10-19 09:23
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AccountQuotaResponseDTO {

    /**
     * 总量额度
     */
    private Integer totalQuota;
    /**
     * 剩余额度
     */
    private Integer surplusQuota;

}
