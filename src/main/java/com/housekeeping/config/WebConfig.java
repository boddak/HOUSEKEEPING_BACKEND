package com.housekeeping.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .exposedHeaders("Set-Cookie", "access", "Authorization")
                .allowedOrigins("http://192.168.0.42:5000", "https://socket.bit-two.com", "https://re.bit-two.com", "http://localhost:5173")
                .exposedHeaders("Set-Cookie", "access")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
                .allowedHeaders("*")
                .allowCredentials(true) // 자격 증명을 허용하도록 설정
                .maxAge(3600L);
    }
}
