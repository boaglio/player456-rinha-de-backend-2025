package com.boaglio.player456;

import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Locale;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @PostConstruct
    public void setDefaultLocale() {
        Locale.setDefault(Locale.US);
    }
}