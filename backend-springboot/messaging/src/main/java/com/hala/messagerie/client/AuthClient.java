package com.hala.messagerie.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
@FeignClient(name = "authentification-messagerie", url = "http://localhost:8070/api/v1/users", contextId = "authentificationClientMessagerie")
public interface AuthClient {
    @GetMapping("/find/{user-id}")
    UserDTO getUserById(@PathVariable("user-id") Integer userId);
}


