package com.hala.authentification.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "gateway", url = "http://localhost:8222/post/api/v1/comments")
public interface CommentClient {
    @GetMapping("/user/{userId}")
    List<CommentDTO> findAllCommentsByUser(@PathVariable("userId") Integer userId);

    @PostMapping("/addComment/{userId}/{postId}")
    CommentDTO addComment(@PathVariable("userId") Integer userId,
                          @PathVariable("postId") Integer postId,
                          @RequestBody CommentDTO commentDTO);

    @PutMapping("/{commentId}")
    CommentDTO updateComment(@PathVariable("commentId") Integer commentId,
                             @RequestParam("content") String content);

    @DeleteMapping("/{commentId}")
    void deleteComment(@PathVariable("commentId") Integer commentId);
}