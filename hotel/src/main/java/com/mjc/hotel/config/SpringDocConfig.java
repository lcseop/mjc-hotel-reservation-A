package com.mjc.hotel.config;

import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpringDocConfig {

    @Bean
    public static BeanFactoryPostProcessor removeQuerydslCustomizer() {
        return beanFactory -> {
            if (beanFactory instanceof DefaultListableBeanFactory dlbf) {
                if (dlbf.containsBeanDefinition("queryDslQuerydslPredicateOperationCustomizer")) {
                    dlbf.removeBeanDefinition("queryDslQuerydslPredicateOperationCustomizer");
                }
            }
        };
    }
}
