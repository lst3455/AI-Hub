package org.example.ai.chatbot.domain.account.service;


import org.example.ai.chatbot.domain.account.model.valobj.AccountQuotaVO;


public interface IAccountQueryService {

    AccountQuotaVO queryAccountQuota(String openid);

}
