package com.hala.authentification.config;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import jakarta.annotation.PostConstruct;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity(securedEnabled = true)
public class SecurityConfiguration {
    @PostConstruct
    public void init() {
        System.out.println("✅ SecurityConfiguration LOADED by Spring !");
    }
    private static final String[] WHITE_LIST_URL = {
            "/api/v1/auth/**",
            "/api/v1/approve",
            "/api/v1/manage-access",
            "/authentication/api/v1/auth/refresh-token",
            "/api/v1/authenticate",
            "/v3/api-docs/**",
            "/v3/api-docs.yaml",
            "/swagger-ui/**",
            "/swagger-ui.html",             // ← AJOUT
            "/swagger-resources/**",        // ← AJOUT
            "/webjars/**",
            "/api/group-messages/**",
            "/likes/**",
            "/notifications/**",
            "/api/private-messages/**",
            "/shares/**",
            "/api/v1/users/create",
            "/api/v1/users/**",
            "/api/v1/users/update/{userId}",
            "/api/v1/posts/{userId}",
            "/api/v1/posts/create-post",
            "/post/api/v1/posts/create-post",
            "/authentication/api/v1/auth/**",
            "/authentication/api/v1/users/find/{userId}",
            "/post/api/v1/posts/**",
            "/post/api/v1/comments/**",
            "/post/api/v1/shares/**",
            "/api/v1/shares/**",
            "/post/api/v1/likes/**",
            "/api/v1/likes/**",
            "/messaging/api/v1/groupmessages/**",
            "/messaging/api/v1/messages/**",
            "/messaging/api/v1/private-messages/**",
            "/authentication/api/v1/users/*/upload-profile-photo",
            "/post/api/v1/comments/addComment/{userId}/{postId}",
            "/api/v1/users/find/{userId}",
            "/authentication/api/v1/users",
            "/post/api/v1/events/**",



    };
    private final JwtAuthenticationFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(req -> req
                        .anyRequest().permitAll()  // 🔥 TEMPORAIRE : autorise tout sans sécurité
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }


}