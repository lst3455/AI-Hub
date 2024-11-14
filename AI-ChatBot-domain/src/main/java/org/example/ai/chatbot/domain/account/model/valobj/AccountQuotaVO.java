package org.example.ai.chatbot.domain.account.model.valobj;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * @author Fuzhengwei bugstack.cn @小傅哥
 * @description 账户额度值对象
 * @create 2024-10-06 08:56
 */
@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class AccountQuotaVO {

    /**
     * 总量额度
     */
    private Integer totalQuota;
    /**
     * 剩余额度
     */
    private Integer surplusQuota;

}
