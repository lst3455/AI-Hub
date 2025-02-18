package org.example.ai.chatbot.domain.account.adapter.repository;


import org.example.ai.chatbot.domain.account.model.entity.AdjustQuotaEntity;
import org.example.ai.chatbot.domain.account.model.valobj.AccountQuotaVO;


public interface IAccountRepository {

    AccountQuotaVO adjustQuota(AdjustQuotaEntity adjustQuotaEntity);

    AccountQuotaVO queryAccountQuota(String openid);

}
