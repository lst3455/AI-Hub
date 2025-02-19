package org.example.ai.chatbot.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;


@Data
@ConfigurationProperties(prefix = "chatglm.sdk.config", ignoreInvalidFields = true)
public class ChatGPTSDKConfigProperties {

    /** 转发地址 <a href="https://open.bigmodel.cn/">https://api.xfg.im/b8b6/</a> */
    private String apiHost;
    /** d847050042570e7136f926a7d3cc3e83.UMIVEHtoforykzFU */
    private String apiKey;
//    /** 获取Token <a href="http://api.xfg.im:8080/authorize?username=xfg&password=123">访问获取</a> */
//    private String authToken;

}
