package com.kunalsumbly.proxysidecar.handler;

import com.kunalsumbly.proxysidecar.config.ProxyConfiguration;

import lombok.extern.java.Log;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.transport.ProxyProvider;

import java.nio.file.Files;
import java.nio.file.Paths;

@Component
@Slf4j
public class ProxyHandler {

    private final WebClient webClient;
    private final ProxyConfiguration proxyConfiguration;
    private final ResourceLoader resourceLoader;

    @Autowired
    public ProxyHandler(WebClient.Builder webClientBuilder, ProxyConfiguration proxyConfiguration, ResourceLoader resourceLoader) {
        this.webClient = webClientBuilder.build();
        this.proxyConfiguration = proxyConfiguration;
        this.resourceLoader = resourceLoader;
    }

    public Mono<String> handleRequest(String path) {
        log.info("Handling request for path: " + path);
        // Check if the path is a local file mapping
//        String normalizedPath = path.startsWith("/") ? path.substring(1) : path;
//        String localFilePath = proxyConfiguration.getUrlMappings().get(normalizedPath);
//        if (localFilePath != null) {
//            // Read response from local file store
//            // This is a blocking operation, so wrap it in a Mono.fromCallable
//            return Mono.fromCallable(() -> new String(Files.readAllBytes(Paths.get(resourceLoader.getResource("classpath:" + localFilePath).getURI()))));
//        } else {
            // Forward request to downstream service
        return webClient.get()
                .uri(uriBuilder ->
                        uriBuilder.scheme("http").path(path).build())
                .retrieve()
                .bodyToMono(String.class);
        //}
    }

}