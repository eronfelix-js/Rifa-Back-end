package dev.Felix.rifa_system.Security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import java.io.IOException;

/**
 * Handler customizado para erros de requisições HTTP
 */
@Slf4j
public class CustomErrorHandler extends DefaultResponseErrorHandler {

    @Override
    public void handleError(ClientHttpResponse response) throws IOException {
        HttpStatus statusCode = (HttpStatus) response.getStatusCode();

        log.error("Erro na requisição HTTP - Status: {} - {}",
                statusCode.value(), statusCode.getReasonPhrase());

        switch (statusCode.series()) {
            case CLIENT_ERROR:
                log.error("Erro 4xx - Client Error");
                throw new HttpClientErrorException(
                        statusCode,
                        response.getStatusText()
                );

            case SERVER_ERROR:
                log.error("Erro 5xx - Server Error");
                throw new HttpServerErrorException(
                        statusCode,
                        response.getStatusText()
                );

            default:
                super.handleError(response);
        }
    }
}
