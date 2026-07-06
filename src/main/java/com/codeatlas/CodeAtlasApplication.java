package com.codeatlas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

@SpringBootApplication
public class CodeAtlasApplication {

    public static void main(String[] args) {
        SpringApplication.run(CodeAtlasApplication.class, args);
    }

    @Bean
    public RestClientCustomizer restClientCustomizer() {
        return restClientBuilder -> {
            SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
            requestFactory.setConnectTimeout(2000); // 2 seconds
            requestFactory.setReadTimeout(60000);    // 60 seconds
            restClientBuilder.requestFactory(requestFactory);
        };
    }
}
