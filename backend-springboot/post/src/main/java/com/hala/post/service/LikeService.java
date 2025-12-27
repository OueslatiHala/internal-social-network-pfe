package com.hala.post.service;

import com.hala.post.client.AuthClient;
import com.hala.post.client.UserDTO;
import com.hala.post.dto.LikeDTO;
import com.hala.post.dto.NotificationDTO;
import com.hala.post.entities.Like;
import com.hala.post.entities.Post;
import com.hala.post.enumm.NotificationType;
import com.hala.post.mappers.LikeMapper;
import com.hala.post.repository.LikeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LikeService {

    @Autowired private LikeRepository likeRepository;
    @Autowired private PostService postService;
    @Autowired private LikeMapper likeMapper;
    @Autowired private AuthClient authClient;
    @Autowired private KafkaTemplate<String, NotificationDTO> kafkaTemplate;

    private static final String TOPIC = "notifications-topic";
    @Transactional
    public LikeDTO likePost(Integer userId, Integer postId) {
        if (likeRepository.existsByUserIdAndPostId(userId, postId)) {
            throw new IllegalStateException("Already liked by this user.");
        }

        Post post = postService.findPostEntityById(postId);
        Like like = Like.builder()
                .post(post)
                .userId(userId)
                .dateLike(LocalDateTime.now())
                .build();
        Like savedLike = likeRepository.save(like);

        // SEULEMENT si le likeur n'est pas l'auteur du post
        if (!userId.equals(post.getUserId())) {
            UserDTO sender = authClient.getUserById(userId);
            String senderName = sender.getRole().name().equals("PARTENAIRE") ? sender.getCompanyName() : sender.getFirstname() + " " + sender.getLastname();

            NotificationDTO notification = NotificationDTO.builder()
                    .userId(post.getUserId())
                    .senderId(userId)
                    .postId(postId.toString())
                    .type(NotificationType.LIKE)
                    .message(senderName + " liked your post.")
                    .createdAt(LocalDateTime.now())
                    .read(false)
                    .senderName(senderName)
                    .build();

            kafkaTemplate.send(TOPIC, notification);
        }

        return likeMapper.likeToLikeDto(savedLike);
    }


    public List<LikeDTO> getAllLikes() {
        return likeRepository.findAll().stream()
                .map(likeMapper::likeToLikeDto)
                .collect(Collectors.toList());
    }

    public List<LikeDTO> getLikesByPostId(Integer postId) {
        return likeRepository.findAllByPostId(postId).stream()
                .map(likeMapper::likeToLikeDto)
                .collect(Collectors.toList());
    }

    public Long countLikesForPost(Integer postId) {
        return likeRepository.countByPostId(postId);
    }

    public boolean deleteLike(Integer id) {
        if (likeRepository.existsById(id)) {
            likeRepository.deleteById(id);
            return true;
        }
        return false;
    }

    public List<UserDTO> getUsersWhoLikedPost(Integer postId) {
        return likeRepository.findByPostId(postId).stream()
                .map(like -> authClient.getUserById(like.getUserId()))
                .collect(Collectors.toList());
    }
}