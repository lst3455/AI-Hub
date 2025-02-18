package org.example.ai.chatbot.config;

import com.github.houbb.sensitive.word.bs.SensitiveWordBs;
import com.github.houbb.sensitive.word.utils.InnerWordCharUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Slf4j
@Configuration
public class SensitiveWordConfig {

    @Bean
    public SensitiveWordBs sensitiveWordBs() {
        return SensitiveWordBs.newInstance()
                .wordReplace((stringBuilder, chars, wordResult, iWordContext) -> {
                    String sensitiveWord = InnerWordCharUtils.getString(chars, wordResult);
                    log.info("Detected sensitive word: {}", sensitiveWord);
                    // Replace operation - specify replacement with "*" or other characters if desired
                })
                .ignoreCase(true) // Case insensitive
                .ignoreWidth(true) // Ignore full-width and half-width characters
                .enableWordCheck(true) // Only enable word filtering
                .init();
    }
}
