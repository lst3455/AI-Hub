package org.example.ai.chatbot.infrastructure.repository;


import org.example.ai.chatbot.domain.openai.model.entity.UserAccountEntity;
import org.example.ai.chatbot.domain.openai.model.valobj.UserAccountStatusVO;
import org.example.ai.chatbot.domain.openai.repository.IOpenAiRepository;
import org.example.ai.chatbot.infrastructure.dao.IUserAccountDao;
import org.example.ai.chatbot.infrastructure.po.UserAccountPO;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;

/**
 * @author Fuzhengwei bugstack.cn @小傅哥
 * @description OpenAi 仓储服务
 * @create 2023-10-03 17:14
 */
@Repository
public class OpenAiRepository implements IOpenAiRepository {

    @Resource
    private IUserAccountDao userAccountDao;

    @Override
    public int subAccountQuota(String openid) {
        return userAccountDao.subAccountQuota(openid);
    }

    @Override
    public UserAccountEntity queryUserAccount(String openid) {
        UserAccountPO userAccountPO = userAccountDao.queryUserAccount(openid);
        if (null == userAccountPO) return null;
        UserAccountEntity userAccountEntity = new UserAccountEntity();
        userAccountEntity.setOpenid(userAccountPO.getOpenid());
        userAccountEntity.setTotalQuota(userAccountPO.getTotalQuota());
        userAccountEntity.setSurplusQuota(userAccountPO.getSurplusQuota());
        userAccountEntity.setUserAccountStatusVO(UserAccountStatusVO.get(userAccountPO.getStatus()));
        userAccountEntity.genModelTypes(userAccountPO.getModelTypes());

        return userAccountEntity;
    }

    @Override
    public void insertUserAccount(String openid) {
        UserAccountPO userAccountPO = new UserAccountPO();
        userAccountPO.setOpenid(openid);
        userAccountPO.setStatus(0);
        userAccountPO.setModelTypes("");
        userAccountPO.setTotalQuota(0);
        userAccountPO.setSurplusQuota(0);

        userAccountDao.insertUserAccount(userAccountPO);
    }

}
