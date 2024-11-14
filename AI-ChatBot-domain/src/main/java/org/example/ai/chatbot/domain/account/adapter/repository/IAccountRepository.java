package org.example.ai.chatbot.domain.account.adapter.repository;


import org.example.ai.chatbot.domain.account.model.entity.AdjustQuotaEntity;
import org.example.ai.chatbot.domain.account.model.valobj.AccountQuotaVO;

/**
 * @author Fuzhengwei bugstack.cn @小傅哥
 * @description 账户仓储服务
 * @create 2024-10-06 09:11
 */
public interface IAccountRepository {

    AccountQuotaVO adjustQuota(AdjustQuotaEntity adjustQuotaEntity);

    AccountQuotaVO queryAccountQuota(String openid);

}
