package org.example.ai.chatbot.domain.account.service.query;


import org.example.ai.chatbot.domain.account.adapter.repository.IAccountRepository;
import org.example.ai.chatbot.domain.account.model.valobj.AccountQuotaVO;
import org.example.ai.chatbot.domain.account.service.IAccountQueryService;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;


@Service
public class AccountQueryService implements IAccountQueryService {

    @Resource
    private IAccountRepository repository;

    @Override
    public AccountQuotaVO queryAccountQuota(String openid) {
        return repository.queryAccountQuota(openid);
    }

}
