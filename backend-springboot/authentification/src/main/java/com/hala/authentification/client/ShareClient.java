package com.hala.authentification.client;

import com.hala.post.dto.ShareDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@FeignClient(name = "gateway", url = "http://localhost:8222/post/api/v1")
public interface ShareClient {

    @GetMapping("/shares/user/{userId}")
    List<ShareDTO> findAllSharesByUser(@PathVariable("userId") Integer userId);

    @PostMapping("/posts/{postId}/share/{userId}")
    ShareDTO sharePost(@PathVariable("postId") Integer postId, @PathVariable("userId") Integer userId);
}
