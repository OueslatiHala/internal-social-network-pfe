package com.hala.post.service;

import com.hala.post.client.AuthClient;
import com.hala.post.client.UserDTO;
import com.hala.post.dto.NotificationDTO;
import com.hala.post.dto.ShareDTO;
import com.hala.post.entities.Post;
import com.hala.post.entities.Share;
import com.hala.post.enumm.NotificationType;
import com.hala.post.enumm.UserRole;
import com.hala.post.mappers.ShareMapper;
import com.hala.post.repository.ShareRepository;
import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ShareService {

    private final ShareRepository shareRepository;
    private final PostService postService;
    private final ShareMapper shareMapper;
    private final AuthClient authClient;
    private final KafkaTemplate<String, NotificationDTO> kafkaTemplate;
    private static final Logger logger = LoggerFactory.getLogger(ShareService.class);
    private static final String TOPIC = "notifications-topic";

    @Autowired
    public ShareService(ShareRepository shareRepository, @Lazy PostService postService,
                        ShareMapper shareMapper, AuthClient authClient,
                        KafkaTemplate<String, NotificationDTO> kafkaTemplate) {
        this.shareRepository = shareRepository;
        this.postService = postService;
        this.shareMapper = shareMapper;
        this.authClient = authClient;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Transactional
    public ShareDTO sharePost(Integer userId, Integer postId, String additionalContent) {
        logger.info("Trying to share postId={} by userId={}", postId, userId);

        Post post;
        try {
            post = postService.findPostEntityById(postId);
        } catch (EntityNotFoundException e) {
            logger.error("❌ Cannot share post: post {} not found", postId);
            throw new IllegalArgumentException("Cannot share: original post no longer exists.");
        }

        Share share = new Share();
        share.setDateShare(new Date());
        share.setUserId(userId);
        share.setPost(post);
        share.setAdditionalContent(additionalContent != null ? additionalContent : "");

        Share savedShare = shareRepository.save(share);

        // Retrieve sharer details
        UserDTO sender;
        try {
            sender = authClient.getUserById(userId);
        } catch (FeignException e) {
            logger.error("Failed to fetch user details for sharer ID: {}", userId);
            throw new EntityNotFoundException("User not found with id: " + userId);
        }

        String senderName = (sender.getRole() == UserRole.PARTENAIRE && sender.getCompanyName() != null)
                ? sender.getCompanyName()
                : sender.getFirstname() + " " + sender.getLastname();

        // ✅ Notification uniquement si ce n’est pas soi-même
        if (!userId.equals(post.getUserId())) {
            NotificationDTO notificationDTO = NotificationDTO.builder()
                    .userId(post.getUserId())
                    .senderId(userId)
                    .postId(postId.toString())
                    .type(NotificationType.SHARE)
                    .message(senderName + " shared your post.")
                    .createdAt(LocalDateTime.now())
                    .read(false)
                    .senderName(senderName)
                    .build();

            kafkaTemplate.send(TOPIC, notificationDTO);
        }

        return shareMapper.shareToShareDto(savedShare);
    }


    public List<ShareDTO> findAllSharesByPostId(Integer postId) {
        return shareRepository.findAllByPostId(postId).stream()
                .map(shareMapper::shareToShareDto)
                .collect(Collectors.toList());
    }

    public ShareDTO getShareById(Integer shareId) {
        Share share = shareRepository.findById(shareId)
                .orElseThrow(() -> new EntityNotFoundException("Share not found with id: " + shareId));
        return shareMapper.shareToShareDto(share);
    }


    public List<ShareDTO> getSharesByPostId(Integer postId) {
        List<Share> shares = shareRepository.findByPostId(postId);

        return shares.stream().map(share -> {
            ShareDTO dto = shareMapper.shareToShareDto(share);
            // Appel à ton client Feign pour aller chercher l'utilisateur
            try {
                UserDTO user = authClient.getUserById(share.getUserId());
                dto.setUser(user);
            } catch (Exception e) {
                dto.setUser(null);
            }
            return dto;
        }).toList();
    }

    public List<ShareDTO> getAllShares() {
        return shareRepository.findAll().stream()
                .map(shareMapper::shareToShareDto)
                .collect(Collectors.toList());
    }

    public List<ShareDTO> findAllSharesByUser(Integer userId) {
        return shareRepository.findAllByUserId(userId).stream()
                .map(shareMapper::shareToShareDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteShare(Integer shareId) {
        Share share = shareRepository.findById(shareId)
                .orElseThrow(() -> new EntityNotFoundException("Share not found with id: " + shareId));
        shareRepository.delete(share);
    }


    @Transactional
    public List<UserDTO> getSharedUsersForPost(Integer postId) {
        return shareRepository.findAllByPostId(postId).stream()
                .map(Share::getUserId)
                .distinct()
                .map(userId -> {
                    try {
                        UserDTO user = authClient.getUserById(userId);

                        if (user.getLogo() != null && !user.getLogo().startsWith("http")) {
                            user.setLogo("http://localhost:8070/api/v1/users/downloadFile/" + user.getLogo());
                        }

                        if (user.getProfilePicture() != null && !user.getProfilePicture().startsWith("http")) {
                            user.setProfilePicture("http://localhost:8070/api/v1/users/downloadFile/" + user.getProfilePicture());
                        }

                        return user;
                    } catch (FeignException e) {
                        logger.error("Error fetching user with ID: {}", userId, e);
                        UserDTO fallback = new UserDTO();
                        fallback.setId(userId);
                        fallback.setFirstname("Unknown");
                        fallback.setLastname("Unknown");
                        fallback.setCompanyName("Unknown");
                        return fallback;
                    }
                })
                .collect(Collectors.toList());
    }
}
