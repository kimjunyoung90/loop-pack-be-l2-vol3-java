package com.loopers.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class PaymentGatewayConfig {

    @Value("${pg.base-url}")
    private String baseUrl;

    @Value("${pg.connect-timeout}")
    private int connectTimeout;

    @Value("${pg.read-timeout}")
    private int readTimeout;

    @Bean
    public RestClient pgRestClient() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(connectTimeout);
        factory.setReadTimeout(readTimeout);

        return RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(factory)
                .build();
    }
}
