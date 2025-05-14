package org.example.ai.chatbot;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.ImportResource;


@SpringBootApplication
@Configurable
@EnableAspectJAutoProxy(proxyTargetClass = true)
@ImportResource(locations = {"classpath:spring-config.xml"})
public class Application {

    public static void main(String[] args){
        SpringApplication.run(Application.class);
    }

}
