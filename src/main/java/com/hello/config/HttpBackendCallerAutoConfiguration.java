package com.hello.config;


import com.hello.demo.HttpBackendCaller;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnClass(HttpBackendCaller.class)
public class HttpBackendCallerAutoConfiguration {

    /*@Bean
    @ConditionalOnMissingBean
    public HttpBackendCaller getHttpBackendCaller(String backedRegistryBaseUrl){
        return new HttpBackendCaller(backedRegistryBaseUrl);
    }*/

    @Bean
    @ConditionalOnMissingBean
    public HttpBackendCaller getHttpBackendCaller(){
        return new HttpBackendCaller();
    }
}
