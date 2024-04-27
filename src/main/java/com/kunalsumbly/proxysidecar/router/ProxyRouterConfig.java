package com.kunalsumbly.proxysidecar.router;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.net.URI;

import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class ProxyRouterConfig {

    private final WebClient webClient;

    public ProxyRouterConfig(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    @Bean
    public RouterFunction<ServerResponse> proxyRoutes() {
        return route(RequestPredicates.all(), this::forwardRequest);
    }

    private Mono<ServerResponse> forwardRequest(ServerRequest serverRequest) {
        URI originalUri = serverRequest.uri();
        HttpMethod method = serverRequest.method();
        System.out.println("original URL : "+originalUri);
        Mono<?> bodyToForward;
        // Check if the Content-Type is for form data
        if (MediaType.APPLICATION_FORM_URLENCODED
                     .equalsTypeAndSubtype(serverRequest.headers().contentType().orElse(MediaType.APPLICATION_JSON))) {
            // Extract form data and then forward it
            bodyToForward = serverRequest.formData().map(formData -> formData);
        } else {
            // For JSON and other types of bodies, simply forward the raw body
            bodyToForward = serverRequest.bodyToMono(String.class);
        }
        // what if there is no body ?? we are simply doing a GET using query param?
        return bodyToForward.doOnNext(x-> System.out.println("request body::::::::::::"+x)).flatMap(body ->
                webClient.method(method)
                        .uri(originalUri)
                        .headers(headers -> headers.addAll(serverRequest.headers().asHttpHeaders()))
                        .bodyValue(body)
                        .retrieve()
                        .bodyToMono(String.class)
                        .doOnSuccess(x -> System.out.println("response received:::::"+x))
                        .flatMap(x -> ServerResponse.status(HttpStatus.OK).bodyValue(x))
                        .doOnError(x -> System.out.println("error ::::: nothing to do"))
                ).onErrorResume(error -> {
                    System.out.println("Error during forwarding the request: "+ error);
                    // Depending on the error, you might want to return different status codes
                    return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue("An error occurred: " + error.getMessage());
        }).switchIfEmpty(// case for when there is no body and we are only doing GET using params
               webClient.method(method)
                .uri(originalUri)
                .headers(headers -> headers.addAll(serverRequest.headers().asHttpHeaders()))
                .retrieve()
                .bodyToMono(String.class)
                 .doOnNext(x-> System.out.println("doing something here:::::"+x))
                 .flatMap(x -> ServerResponse.status(HttpStatus.OK).bodyValue(x))
        );



    }
}
