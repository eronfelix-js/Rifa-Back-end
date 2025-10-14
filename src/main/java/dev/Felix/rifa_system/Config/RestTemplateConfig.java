package dev.Felix.rifa_system.Config;

import dev.Felix.rifa_system.Security.CustomErrorHandler;
import dev.Felix.rifa_system.Security.LoggingInterceptor;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Configuration
public class RestTemplateConfig {

    /**
     * RestTemplate padrão com configurações otimizadas
     * Usando API não-deprecada do Spring Boot 3.x
     */
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
                .requestFactory(() -> clientHttpRequestFactory())  // Factory customizada
                .interceptors(new LoggingInterceptor())             // Interceptor de logs
                .errorHandler(new CustomErrorHandler())             // Handler de erros
                .build();
    }

    /**
     * Factory para requisições HTTP com buffer e timeouts configurados
     * Permite ler o response body múltiplas vezes
     */
    private ClientHttpRequestFactory clientHttpRequestFactory() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();

        // Configurar timeouts usando a nova API
        factory.setConnectTimeout(Duration.ofSeconds(30));  // Timeout de conexão
        factory.setReadTimeout(Duration.ofSeconds(30));     // Timeout de leitura

        // BufferingClientHttpRequestFactory permite ler o response body múltiplas vezes
        return new BufferingClientHttpRequestFactory(factory);
    }
}