package com.hala.authentification.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "gateway", url = "http://localhost:8222")
public interface LikeClient {
    @GetMapping("post/api/v1/likes")
    List<LikeDTO> findAllLikesByUser(@PathVariable("user-id") Integer userId);
}


