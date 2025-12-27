package com.hala.post.controller;

import com.hala.post.client.UserDTO;
import com.hala.post.dto.PostDTO;
import com.hala.post.dto.PostWithUserDetailsDTO;
import com.hala.post.service.PostService;
import com.hala.post.service.ShareService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile; // <--- Ajoute ceci
import jakarta.validation.Valid;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path; // <--- Ajoute ceci
import java.nio.file.Paths; // <--- Ajoute ceci
import java.nio.file.StandardCopyOption; // <--- Ajoute ceci
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;


@RestController
@RequestMapping("/api/v1/posts")
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
public class PostController {

    @Autowired
    private PostService postService;
    private ShareService shareService;

    @PostMapping("/create-post")
    public ResponseEntity<PostDTO> createPost(@Valid @RequestBody PostDTO postDTO) {
        System.out.println("=====> POST DTO reçu: " + postDTO); // Ajoute cette ligne ici !
        PostDTO createdPost = postService.createPost(postDTO);
        return ResponseEntity.ok(createdPost);
    }
    @GetMapping("/{postId}/shared-users")
    public ResponseEntity<List<UserDTO>> getSharedUsersForPost(@PathVariable Integer postId) {
        List<UserDTO> users = shareService.getSharedUsersForPost(postId);
        return ResponseEntity.ok(users);
    }


    @GetMapping("/user/{userId}")
    public ResponseEntity<List<PostDTO>> getPostsByUserId(
            @PathVariable Integer userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        List<PostDTO> posts = postService.getPostsByUserId(userId, page, size);
        return ResponseEntity.ok(posts);
    }


    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            // Stockage local, modifie selon ton besoin
            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path path = Paths.get("file_storage/" + fileName);
            Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

            // Renvoie l’URL d’accès
            Map<String, String> response = new HashMap<>();
            response.put("url", fileName);
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{postId}")
    public ResponseEntity<PostDTO> getPost(@PathVariable Integer postId) {
        PostDTO post = postService.findPostById(postId);
        return ResponseEntity.ok(post);
    }
    @PutMapping("/{postId}")
    public ResponseEntity<PostDTO> updatePost(
            @PathVariable Integer postId,
            @Valid @RequestBody PostDTO postDTO) {
        PostDTO updatedPost = postService.updatePost(postId, postDTO);
        return ResponseEntity.ok(updatedPost);
    }
    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> deletePost(@PathVariable Integer postId) {
        postService.deletePost(postId);
        return ResponseEntity.noContent().build();
    }
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        return ResponseEntity.ok(postService.getPostStats());
    }


    @GetMapping("/with-user-details")
    public ResponseEntity<List<PostWithUserDetailsDTO>> getPostsWithUserDetails(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        List<PostWithUserDetailsDTO> posts = postService.getAllPostsWithUserDetails(page, size);
        return ResponseEntity.ok(posts);
    }
    @GetMapping("/top-liked")
    public ResponseEntity<List<PostWithUserDetailsDTO>> getTopLikedPosts(@RequestParam(defaultValue = "5") int limit) {
        return ResponseEntity.ok(postService.getTopLikedPosts(limit));
    }


}
