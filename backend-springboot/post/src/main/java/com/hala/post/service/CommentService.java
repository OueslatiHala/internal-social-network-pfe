package com.hala.post.service;

import com.hala.post.client.AuthClient;
import com.hala.post.client.UserDTO;
import com.hala.post.dto.CommentDTO;
import com.hala.post.dto.NotificationDTO;
import com.hala.post.entities.Comment;
import com.hala.post.entities.Post;
import com.hala.post.enumm.NotificationType;
import com.hala.post.mappers.CommentMapper;
import com.hala.post.repository.CommentRepository;
import com.hala.post.repository.PostRepository;
import feign.FeignException;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class CommentService {

    private final CommentRepository commentRepository;
    private final CommentMapper commentMapper;
    private final PostRepository postRepository;
    private final AuthClient authClient;
    private final KafkaTemplate<String, NotificationDTO> kafkaTemplate;
    private static final Logger logger = LoggerFactory.getLogger(CommentService.class);
    private static final String TOPIC = "notifications-topic";

    @Autowired
    public CommentService(CommentRepository commentRepository, CommentMapper commentMapper,
                          PostRepository postRepository, AuthClient authClient,
                          KafkaTemplate<String, NotificationDTO> kafkaTemplate) {
        this.commentRepository = commentRepository;
        this.commentMapper = commentMapper;
        this.postRepository = postRepository;
        this.authClient = authClient;
        this.kafkaTemplate = kafkaTemplate;
    }

    public CommentDTO addComment(Integer userId, Integer postId, String content) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("Post not found with id: " + postId));

        Comment comment = new Comment();
        comment.setContent(content);
        comment.setDateComment(LocalDateTime.now());
        comment.setUserId(userId);
        comment.setPost(post);

        Comment savedComment = commentRepository.save(comment);

        UserDTO sender;
        try {
            sender = authClient.getUserById(userId);
        } catch (FeignException e) {
            logger.error("Failed to fetch user details for commenter ID: {}", userId);
            throw new EntityNotFoundException("User not found with id: " + userId);
        }

        String senderName = (sender.getRole() != null && sender.getRole().name().equals("PARTENAIRE"))
                ? sender.getCompanyName()
                : sender.getFirstname() + " " + sender.getLastname();

        // 👉 Vérifie que le commentateur est différent du propriétaire du post
        if (!userId.equals(post.getUserId())) {
            NotificationDTO notificationDTO = new NotificationDTO(
                    post.getUserId(),
                    userId,
                    postId.toString(),
                    NotificationType.COMMENT,
                    senderName + " commented on your post.",
                    LocalDateTime.now(),
                    false,
                    senderName
            );

            kafkaTemplate.send(TOPIC, notificationDTO);
        }


        return commentMapper.commentToCommentDto(savedComment);
    }

    public CommentDTO updateComment(Integer commentId, String content) {
        Comment existingComment = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("Comment not found with id: " + commentId));

        existingComment.setContent(content);
        return commentMapper.commentToCommentDto(commentRepository.save(existingComment));
    }

    public void deleteComment(Integer commentId) {
        if (!commentRepository.existsById(commentId)) {
            throw new EntityNotFoundException("Comment not found with id: " + commentId);
        }
        commentRepository.deleteById(commentId);
    }

    public List<CommentDTO> getAllComments() {
        return commentRepository.findAll().stream()
                .map(commentMapper::commentToCommentDto)
                .collect(Collectors.toList());
    }

    public List<CommentDTO> getCommentsByPostId(Integer postId) {
        return commentRepository.findAllByPostId(postId).stream()
                .map(commentMapper::commentToCommentDto)
                .collect(Collectors.toList());
    }
}
