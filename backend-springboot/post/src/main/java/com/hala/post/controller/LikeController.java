package com.hala.post.controller;

import com.hala.post.client.UserDTO;
import com.hala.post.dto.LikeDTO;
import com.hala.post.service.LikeService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/likes")
@CrossOrigin(origins = "http://localhost:4200", maxAge = 3600)
public class LikeController {

    @Autowired private LikeService likeService;

    @PostMapping("/{userId}/{postId}")
    public ResponseEntity<LikeDTO> likePost(@PathVariable Integer userId, @PathVariable Integer postId) {
        try {
            LikeDTO likeDTO = likeService.likePost(userId, postId);
            return ResponseEntity.ok(likeDTO);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping
    public ResponseEntity<List<LikeDTO>> getAllLikes() {
        return ResponseEntity.ok(likeService.getAllLikes());
    }

    @GetMapping("/{postId}")
    public ResponseEntity<List<LikeDTO>> getLikesForPost(@PathVariable Integer postId) {
        return ResponseEntity.ok(likeService.getLikesByPostId(postId));
    }

    @GetMapping("/count/{postId}")
    public ResponseEntity<Long> countLikesForPost(@PathVariable Integer postId) {
        return ResponseEntity.ok(likeService.countLikesForPost(postId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLike(@PathVariable Integer id) {
        if (likeService.deleteLike(id)) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/users/{postId}")
    public ResponseEntity<List<UserDTO>> getUsersWhoLikedPost(@PathVariable Integer postId) {
        return ResponseEntity.ok(likeService.getUsersWhoLikedPost(postId));
    }
}