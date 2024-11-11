package org.example.ai.chatbot.infrastructure.dao;


import org.apache.ibatis.annotations.Mapper;
import org.example.ai.chatbot.infrastructure.po.UserAccountPO;

/**
 * @author Fuzhengwei bugstack.cn @小傅哥
 * @description 用户账户DAO
 * @create 2023-10-03 16:39
 */
@Mapper
public interface IUserAccountDao {

    int subAccountQuota(String openid);

    UserAccountPO queryUserAccount(String openid);

    void insertUserAccount(UserAccountPO userAccountPO);
}
