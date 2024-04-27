package com.kunalsumbly.proxysidecar.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.transport.ProxyProvider;

import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "proxy")
public class ProxyConfiguration {

    private Map<String, String> urlMappings = new java.util.HashMap<>();

    public Map<String, String> getUrlMappings() {
        return urlMappings;
    }

    public void setUrlMappings(Map<String, String> urlMappings) {
        this.urlMappings = urlMappings;
    }

//    @Bean
//    public WebClient.Builder webClientBuilder() {
//        HttpClient httpClient = HttpClient.create()
//                .proxy(proxy -> proxy
//                        .type(ProxyProvider.Proxy.HTTP)
//                );
//
//        return WebClient.builder()
//                .clientConnector(new ReactorClientHttpConnector(httpClient));
//    }
}