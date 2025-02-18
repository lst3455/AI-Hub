package org.example.ai.chatbot.infrastructure.dao;


import org.apache.ibatis.annotations.Mapper;
import org.example.ai.chatbot.infrastructure.po.UserAccountPO;


@Mapper
public interface IUserAccountDao {

    int subAccountQuota(String openid);

    UserAccountPO queryUserAccount(String openid);

    void insertUserAccount(UserAccountPO userAccountPO);

    int addAccountQuota(UserAccountPO userAccountPOReq);
}
