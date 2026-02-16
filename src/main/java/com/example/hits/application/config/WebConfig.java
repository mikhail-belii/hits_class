package com.example.hits.application.config;

import com.example.hits.application.filter.JwtFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class WebConfig {
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public FilterRegistrationBean<JwtFilter> filterRegistrationBean(JwtFilter jwtFilter) {
        var registrationBean = new FilterRegistrationBean<JwtFilter>();
        registrationBean.setFilter(jwtFilter);
        registrationBean.addUrlPatterns("/api/v1/*");
        return registrationBean;
    }
}
