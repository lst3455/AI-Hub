package org.example.ai.chatbot.config;

import cn.bugstack.chatglm.model.Model;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class ChatGPTModelConfig {

    @Bean
    public Map<String, Model> modelMap(){
        Map<String,Model> modelMap = new HashMap<>();
        modelMap.put("GLM_4",Model.GLM_4);
        modelMap.put("GLM_3_5_TURBO",Model.GLM_3_5_TURBO);
        return modelMap;
    }

}
