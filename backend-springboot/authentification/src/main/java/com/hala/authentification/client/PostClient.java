package com.hala.authentification.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "gateway", url = "http://localhost:8222/post/api/v1")
public interface PostClient {

    @GetMapping("/posts/user/{userId}")
    List<PostDTO> getPostsByUserId(@PathVariable("userId") Integer userId);

    @GetMapping("/post/api/v1/posts")
    List<PostDTO> getAllPosts();

    @PostMapping("/posts/create-post")
    ResponseEntity<PostDTO> createPost(@RequestBody PostDTO postDTO);
}
