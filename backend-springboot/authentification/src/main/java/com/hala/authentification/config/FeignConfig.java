package com.hala.authentification.config;
import com.hala.authentification.auth.AuthenticationRequest;
import com.hala.authentification.auth.AuthenticationResponse;
import com.hala.authentification.auth.AuthenticationService;
import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import lombok.RequiredArgsConstructor;
@Configuration
@RequiredArgsConstructor
public class FeignConfig {
    private final AuthenticationService authService;
    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {
            AuthenticationResponse authResponse = authService.authenticate(
                    new AuthenticationRequest(/* provide authentication details */)
            );
            String token = authResponse.getAccessToken();
            requestTemplate.header("Authorization", "Bearer " + token);
        };
    }
}