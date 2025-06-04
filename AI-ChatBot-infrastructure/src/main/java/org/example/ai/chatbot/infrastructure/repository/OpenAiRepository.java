package org.example.ai.chatbot.infrastructure.repository;


import org.example.ai.chatbot.domain.openai.model.entity.UserAccountEntity;
import org.example.ai.chatbot.domain.openai.model.valobj.UserAccountStatusVO;
import org.example.ai.chatbot.domain.openai.repository.IOpenAiRepository;
import org.example.ai.chatbot.infrastructure.dao.IUserAccountDao;
import org.example.ai.chatbot.infrastructure.po.UserAccountPO;
import org.springframework.stereotype.Repository;

import jakarta.annotation.Resource;


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

//        // get string type of model list
//        StringBuilder modelTypes = new StringBuilder();
//        for (String modelKey : modelMap.keySet()){
//            modelTypes.append(modelKey);
//            modelTypes.append(Constants.SPLIT);
//        }
//        modelTypes.deleteCharAt(modelTypes.length() - 1);

        userAccountPO.setModelTypes("qwen3:1.7b,qwen3:8b,qwen3:14b,glm:4flash,qwen3:235b,qwen3:plus,qwen3:max,deepseek:r1,deepseek:v3");
        userAccountPO.setTotalQuota(3);
        userAccountPO.setSurplusQuota(3);

        userAccountDao.insertUserAccount(userAccountPO);
    }

}
