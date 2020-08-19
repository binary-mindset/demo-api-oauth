package com.oauth.demo.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = "com.oauth.demo")
public class ApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Demo Oauth API")
                        .description("This is an API with Oauth")
                        .version("1.0")
                        .contact(new Contact()
                                .name("Binary Mindset")
                                .url("https://binarymindset.com")
                                .email("binarymindset.blog@gmail.com")
                        )
                );
    }

}

