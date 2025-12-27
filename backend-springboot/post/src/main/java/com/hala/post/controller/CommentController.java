package com.hala.post.controller;

import com.hala.post.client.UserDTO;
import com.hala.post.dto.CommentDTO;
import com.hala.post.entities.Comment;
import com.hala.post.mappers.CommentMapper;
import com.hala.post.repository.CommentRepository;
import com.hala.post.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/comments")
public class CommentController {

    private final CommentService commentService;
    private final CommentMapper commentMapper;
    private final CommentRepository commentRepository;

    @Autowired
    public CommentController(CommentService commentService, CommentMapper commentMapper, CommentRepository commentRepository) {
        this.commentService = commentService;
        this.commentMapper = commentMapper;
        this.commentRepository = commentRepository;
    }

    @PostMapping("/add/{userId}/{postId}")
    public ResponseEntity<CommentDTO> addComment(@PathVariable Integer userId, @PathVariable Integer postId,
                                                 @RequestParam String content) {
        return new ResponseEntity<>(commentService.addComment(userId, postId, content), HttpStatus.CREATED);
    }

    @PutMapping("/{commentId}")
    public ResponseEntity<CommentDTO> updateComment(@PathVariable Integer commentId, @RequestParam String content) {
        return new ResponseEntity<>(commentService.updateComment(commentId, content), HttpStatus.OK);
    }
    @GetMapping("/users/{userId}")
    public ResponseEntity<List<CommentDTO>> getCommentsByUser(@PathVariable Integer userId) {
        List<Comment> comments = commentRepository.findByUserId(userId);
        List<CommentDTO> dtos = comments.stream()
                .map(commentMapper::commentToCommentDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable Integer commentId) {
        commentService.deleteComment(commentId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping
    public ResponseEntity<List<CommentDTO>> getAllComments() {
        return new ResponseEntity<>(commentService.getAllComments(), HttpStatus.OK);
    }

    @GetMapping("/post/{postId}")
    public ResponseEntity<List<CommentDTO>> getCommentsByPostId(@PathVariable Integer postId) {
        return new ResponseEntity<>(commentService.getCommentsByPostId(postId), HttpStatus.OK);
    }
}
