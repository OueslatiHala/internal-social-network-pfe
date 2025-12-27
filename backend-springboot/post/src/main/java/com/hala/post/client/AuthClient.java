package com.hala.post.client;

import feign.FeignException;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "gateway-post", url = "http://localhost:8222/authentication/api/v1", contextId = "gatewayClientPost")
public interface AuthClient {
    @GetMapping("/users/find/{userId}")
    UserDTO getUserById(@PathVariable("userId") Integer userId) throws FeignException;
    @GetMapping("/users/all-ids")
    List<Integer> getAllUserIds();
    @GetMapping("/users/ids-by-role")
    List<Integer> getUserIdsByRole(@RequestParam("role") String role);

}
