package org.example.ai.chatbot.infrastructure.repository;


import cn.bugstack.chatglm.model.Model;
import org.example.ai.chatbot.domain.openai.model.entity.UserAccountEntity;
import org.example.ai.chatbot.domain.openai.model.valobj.UserAccountStatusVO;
import org.example.ai.chatbot.domain.openai.repository.IOpenAiRepository;
import org.example.ai.chatbot.infrastructure.dao.IUserAccountDao;
import org.example.ai.chatbot.infrastructure.po.UserAccountPO;
import org.example.ai.chatbot.types.common.Constants;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.Map;

/**
 * @author Fuzhengwei bugstack.cn @小傅哥
 * @description OpenAi 仓储服务
 * @create 2023-10-03 17:14
 */
@Repository
public class OpenAiRepository implements IOpenAiRepository {

    @Resource
    private IUserAccountDao userAccountDao;

    @Resource
    private Map<String, Model> modelMap;


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

        // get string type of model list
        StringBuilder modelTypes = new StringBuilder();
        for (String modelKey : modelMap.keySet()){
            modelTypes.append(modelKey);
            modelTypes.append(Constants.SPLIT);
        }
        modelTypes.deleteCharAt(modelTypes.length() - 1);

        userAccountPO.setModelTypes(modelTypes.toString());
        userAccountPO.setTotalQuota(3);
        userAccountPO.setSurplusQuota(3);

        userAccountDao.insertUserAccount(userAccountPO);
    }

}
