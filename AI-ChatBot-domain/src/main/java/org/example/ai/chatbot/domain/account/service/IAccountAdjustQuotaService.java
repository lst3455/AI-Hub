package org.example.ai.chatbot.domain.account.service;


import org.example.ai.chatbot.domain.account.model.entity.AdjustQuotaEntity;
import org.example.ai.chatbot.domain.account.model.valobj.AccountQuotaVO;

/**
 * @author Fuzhengwei bugstack.cn @小傅哥
 * @description 账户调额接口
 * @create 2024-10-06 08:52
 */
public interface IAccountAdjustQuotaService {

    /**
     * 调额接口
     *
     * @param adjustQuotaEntity 调额实体对象
     * @return 账户额度
     */
    AccountQuotaVO adjustQuota(AdjustQuotaEntity adjustQuotaEntity);

}
