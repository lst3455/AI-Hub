package org.example.ai.chatbot.infrastructure.repository;


import org.example.ai.chatbot.domain.account.adapter.repository.IAccountRepository;
import org.example.ai.chatbot.domain.account.model.entity.AdjustQuotaEntity;
import org.example.ai.chatbot.domain.account.model.valobj.AccountQuotaVO;
import org.example.ai.chatbot.domain.openai.model.valobj.UserAccountStatusVO;
import org.example.ai.chatbot.infrastructure.dao.IUserAccountDao;
import org.example.ai.chatbot.infrastructure.po.UserAccountPO;
import org.springframework.stereotype.Repository;

import jakarta.annotation.Resource;


@Repository
public class AccountRepository implements IAccountRepository {

    @Resource
    private IUserAccountDao userAccountDao;

    @Override
    public AccountQuotaVO adjustQuota(AdjustQuotaEntity adjustQuotaEntity) {

        // 查询额度
        UserAccountPO userAccountPO = userAccountDao.queryUserAccount(adjustQuotaEntity.getOpenid());
        if (null != userAccountPO){
            // 构建参数
            UserAccountPO userAccountPOReq = new UserAccountPO();
            userAccountPOReq.setOpenid(adjustQuotaEntity.getOpenid());
            userAccountPOReq.setTotalQuota(adjustQuotaEntity.getIncreaseQuota());
            userAccountPOReq.setSurplusQuota(adjustQuotaEntity.getIncreaseQuota());
            // 调整额度
            userAccountDao.addAccountQuota(userAccountPOReq);
        }else {
            // 构建参数
            UserAccountPO userAccountPOReq = new UserAccountPO();
            userAccountPOReq.setOpenid(adjustQuotaEntity.getOpenid());
            userAccountPOReq.setTotalQuota(adjustQuotaEntity.getIncreaseQuota());
            userAccountPOReq.setSurplusQuota(adjustQuotaEntity.getIncreaseQuota());
            userAccountPOReq.setStatus(UserAccountStatusVO.AVAILABLE.getCode());
            userAccountPOReq.setModelTypes("gpt-3.5-turbo,gpt-3.5-turbo-16k,gpt-4,chatglm_lite,chatglm_std,chatglm_pro");
            userAccountDao.insertUserAccount(userAccountPOReq);
        }

        userAccountPO = userAccountDao.queryUserAccount(adjustQuotaEntity.getOpenid());

        // 返回结果
        return AccountQuotaVO.builder()
                .totalQuota(userAccountPO.getTotalQuota())
                .surplusQuota(userAccountPO.getSurplusQuota())
                .build();
    }

    @Override
    public AccountQuotaVO queryAccountQuota(String openid) {
        UserAccountPO userAccountPO = userAccountDao.queryUserAccount(openid);
        if (null == userAccountPO) return AccountQuotaVO.builder().totalQuota(0).surplusQuota(0).build();
        return AccountQuotaVO.builder()
                .totalQuota(userAccountPO.getTotalQuota())
                .surplusQuota(userAccountPO.getSurplusQuota())
                .build();
    }

}
