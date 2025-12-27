package com.hala.authentification.config;
import feign.Response;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignErrorDecoderConfig {

    @Bean
    public ErrorDecoder errorDecoder() {
        return new CustomErrorDecoder();
    }

    public class CustomErrorDecoder implements ErrorDecoder {
        @Override
        public Exception decode(String methodKey, Response response) {
            if (response.status() == 403) {
                return new ForbiddenException("Access denied: " + response.request().url());
            }
            return new Default().decode(methodKey, response);
        }
    }
}

class ForbiddenException extends RuntimeException {
    public ForbiddenException(String message) {
        super(message);
    }
}
