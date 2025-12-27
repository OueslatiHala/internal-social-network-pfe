package com.hala.post.service;

import com.hala.post.client.AuthClient;
import com.hala.post.client.UserDTO;
import com.hala.post.dto.NotificationDTO;
import com.hala.post.dto.PostDTO;
import com.hala.post.dto.PostWithUserDetailsDTO;
import com.hala.post.entities.Comment;
import com.hala.post.entities.Post;
import com.hala.post.entities.Share;
import com.hala.post.enumm.NotificationType;
import com.hala.post.mappers.CommentMapper;
import com.hala.post.mappers.PostMapper;
import com.hala.post.mappers.ShareMapper;
import com.hala.post.repository.CommentRepository;
import com.hala.post.repository.LikeRepository;
import com.hala.post.repository.PostRepository;
import com.hala.post.repository.ShareRepository;
import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import jakarta.persistence.EntityNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PostService {

    private static final Logger logger = LoggerFactory.getLogger(PostService.class);
    private final PostRepository postRepository;
    private final PostMapper postMapper;
    private final CommentMapper commentMapper;

    private final AuthClient authClient;
    private final ShareService shareService;
    private final LikeRepository likeRepository;
    private final ShareRepository shareRepository;
    private final ShareMapper shareMapper;
    private final KafkaTemplate<String, NotificationDTO> kafkaTemplate;
    private final Path fileStorageLocation;
    private final CommentRepository commentRepository;
    @Autowired
    public PostService(PostRepository postRepository,
                       PostMapper postMapper, CommentMapper commentMapper,
                       AuthClient authClient,
                       @Lazy ShareService shareService,
                       LikeRepository likeRepository,
                       ShareRepository shareRepository,
                       ShareMapper shareMapper,
                       KafkaTemplate<String, NotificationDTO> kafkaTemplate, CommentRepository commentRepository) {
        this.postRepository = postRepository;
        this.postMapper = postMapper;
        this.commentMapper = commentMapper;
        this.authClient = authClient;
        this.shareService = shareService;
        this.likeRepository = likeRepository;
        this.shareRepository = shareRepository;
        this.shareMapper = shareMapper;
        this.kafkaTemplate = kafkaTemplate;
        this.commentRepository = commentRepository;
        this.fileStorageLocation = Paths.get("file_storage").toAbsolutePath().normalize();

        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new RuntimeException("Could not create the directory where the uploaded files will be stored.", ex);
        }
    }

    public Post findPostEntityById(Integer postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("Post not found with id: " + postId));
    }

    public PostDTO findPostById(Integer postId) {
        Post post = findPostEntityById(postId);
        return postMapper.postToPostDto(post);
    }
    public List<PostDTO> getPostsByUserId(Integer userId, int page, int size) {
        Page<Post> postPage = postRepository.findByUserId(userId, PageRequest.of(page, size));
        return postPage.stream()
                .map(postMapper::postToPostDto)  // ✅ Méthode correcte
                .collect(Collectors.toList());
    }


    @Transactional
    public PostDTO createPost(PostDTO postDTO) {
        // Vérifie l'existence de l'utilisateur
        UserDTO userDTO = authClient.getUserById(postDTO.getUserId());

        // Construire le senderName selon le rôle
        String senderName = (userDTO.getRole() != null && userDTO.getRole().name().equals("PARTENAIRE"))
                ? userDTO.getCompanyName()
                : userDTO.getFirstname() + " " + userDTO.getLastname();

        // Créer le post à partir du DTO
        Post post = postMapper.postDtoToPost(postDTO);

        // 🔥 AJOUTE CETTE LIGNE ICI (juste après le mapping)
        post.setShares(new ArrayList<>());

        post.setDatePub(LocalDateTime.now());        Post savedPost = postRepository.save(post);

        // Notifier tous les utilisateurs
        List<Integer> userIds = authClient.getAllUserIds();
        for (Integer userId : userIds) {
            if (!userId.equals(postDTO.getUserId())) { // 💡 EXCLUT l’auteur du post
                NotificationDTO notificationDTO = new NotificationDTO(
                        userId,
                        postDTO.getUserId(),
                        savedPost.getId().toString(),
                        NotificationType.POST,
                        senderName + " published a new post.",
                        LocalDateTime.now(),
                        false,
                        senderName
                );
                kafkaTemplate.send("notifications-topic", notificationDTO);
            }
        }


        PostDTO result = postMapper.postToPostDto(savedPost);
        result.setUserFirstname(userDTO.getFirstname());
        result.setUserLastname(userDTO.getLastname());
        result.setCompanyName(userDTO.getCompanyName());
        result.setProfilePicture(userDTO.getProfilePicture());
        result.setLogo(userDTO.getLogo());
        return result;
    }
    public List<PostWithUserDetailsDTO> getTopLikedPosts(int limit) {
        List<Post> allPosts = postRepository.findAll(); // ou une requête triée

        int maxLikes = allPosts.stream()
                .mapToInt(p -> p.getLikes() != null ? p.getLikes().size() : 0)
                .max().orElse(0);

        return allPosts.stream()
                .filter(p -> p.getLikes() != null && p.getLikes().size() == maxLikes)
                .map(post -> {
                    PostWithUserDetailsDTO dto = postMapper.postToPostWithUserDto(post);
                    dto.setLikesCount(post.getLikes().size());
                    dto.setCommentsCount(post.getComments().size());
                    dto.setShareCount(post.getShares().size());
                    dto.setUrl(post.getUrl());
                    dto.setContent(post.getContent());
                    dto.setDatePub(post.getDatePub());

                    try {
                        UserDTO user = authClient.getUserById(post.getUserId());
                        dto.setUserFirstname(user.getFirstname());
                        dto.setUserLastname(user.getLastname());
                        dto.setCompanyName(user.getCompanyName());
                        dto.setRole(user.getRole() != null ? user.getRole().toString() : "UNKNOWN");

                        if ("PARTENAIRE".equalsIgnoreCase(dto.getRole())) {
                            dto.setLogo(cleanLogo(user.getLogo()));
                            dto.setProfilePicture(null);
                        } else {
                            dto.setProfilePicture(user.getProfilePicture());
                            dto.setLogo(null);
                        }
                    } catch (Exception e) {
                        dto.setUserFirstname("Unknown");
                        dto.setUserLastname("User");
                        dto.setCompanyName("Unknown Company");
                        dto.setRole("UNKNOWN");
                        dto.setProfilePicture(null);
                        dto.setLogo(null);
                    }

                    return dto;
                }).limit(limit).toList();
    }

    public Map<String, Object> getPostStats() {
        Map<String, Object> stats = new HashMap<>();

        long totalPosts = postRepository.count();
        long totalLikes = likeRepository.count();
        long totalComments = commentRepository.count();
        long totalShares = shareRepository.count();

        List<Integer> employeeIds = authClient.getUserIdsByRole("EMPLOYE");
        List<Integer> partnerIds = authClient.getUserIdsByRole("PARTENAIRE");

        long postsByEmployees = postRepository.countByUserIdIn(employeeIds);
        long postsByPartners = postRepository.countByUserIdIn(partnerIds);

        stats.put("totalPosts", totalPosts);
        stats.put("totalLikes", totalLikes);
        stats.put("totalComments", totalComments);
        stats.put("totalShares", totalShares);
        stats.put("postsByEmployees", postsByEmployees);
        stats.put("postsByPartners", postsByPartners);

        return stats;
    }


    private String cleanLogo(String rawLogo) {
        if (rawLogo == null || rawLogo.isEmpty()) return null;
        if (rawLogo.startsWith("http")) return rawLogo;
        // Récupère juste le nom du fichier
        String clean = rawLogo.substring(rawLogo.lastIndexOf("/") + 1);
        return "http://localhost:8070/api/v1/users/downloadFile/" + clean;
    }






    @Transactional
    public PostDTO updatePost(Integer postId, PostDTO postDTO) {
        Post existingPost = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("Post not found"));

        // Mise à jour des champs
        existingPost.setContent(postDTO.getContent());

        // Mise à jour de l'URL
        if (postDTO.getUrl() != null && !postDTO.getUrl().isEmpty()) {
            existingPost.setUrl(postDTO.getUrl());
        }

        // Update des collections si besoin
        // Supposons que tu as un CommentMapper commentMapper

        existingPost.getComments().clear();
        if (postDTO.getComments() != null) {
            List<Comment> commentEntities = postDTO.getComments().stream()
                    .map(commentMapper::commentDtoToComment) // Conversion DTO -> entity
                    .collect(Collectors.toList());
            existingPost.getComments().addAll(commentEntities);
        }


        Post savedPost = postRepository.save(existingPost);
        return postMapper.postToPostDto(savedPost);
    }

    @Transactional
    public void deletePost(Integer postId) {
        postRepository.deleteById(postId);
    }

    public List<PostWithUserDetailsDTO> getAllPostsWithUserDetails(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "datePub"));
        Page<Post> postPage = postRepository.findAll(pageable);

        return postPage.getContent().stream()
                .map(post -> {
                    PostWithUserDetailsDTO postDTO = postMapper.postToPostWithUserDto(post);
                    UserDTO userDTO = authClient.getUserById(post.getUserId());

                    String role = userDTO.getRole() != null ? userDTO.getRole().name() : "UNKNOWN";
                    String profilePicture = userDTO.getProfilePicture();
                    String rawLogo = userDTO.getLogo();
                    String logoUrl = null;
                    if (rawLogo != null && !rawLogo.isEmpty()) {
                        // Retire les éventuels préfixes ou répertoires inutiles
                        String cleanLogo = rawLogo
                                .replace("/profile-photos/", "")
                                .replace("logo=", ""); // <-- Ajoute ce .replace ici !

                        logoUrl = cleanLogo.startsWith("http")
                                ? cleanLogo
                                : "http://localhost:8070/api/v1/users/downloadFile/" + cleanLogo;
                    }
                    postDTO.setLogo(logoUrl);






                    postDTO.setUserFirstname(userDTO.getFirstname());
                    postDTO.setUserLastname(userDTO.getLastname());
                    postDTO.setCompanyName(userDTO.getCompanyName());
                    postDTO.setRole(role);
                    if ("PARTENAIRE".equalsIgnoreCase(role)) {
                        postDTO.setLogo(logoUrl);
                        postDTO.setProfilePicture(null);
                    } else {
                        postDTO.setLogo(null);
                        postDTO.setProfilePicture(profilePicture);
                    }


                    if (!post.getShares().isEmpty()) {
                        Share lastShare = post.getShares().get(post.getShares().size() - 1);
                        UserDTO shareUser = authClient.getUserById(lastShare.getUserId());
                        String shareRole = shareUser.getRole() != null ? shareUser.getRole().name() : "UNKNOWN";
                        postDTO.setSharedByFirstname(shareUser.getFirstname());
                        postDTO.setSharedByLastname(shareUser.getLastname());
                        postDTO.setSharedByCompanyName(shareUser.getCompanyName());
                        postDTO.setSharedByProfilePicture(shareUser.getProfilePicture());

                        // CORRECTION ICI : Nettoyage du logo partagé
                        if (shareUser.getLogo() != null && !shareUser.getLogo().isEmpty()) {
                            String cleanLogo = shareUser.getLogo().replace("/profile-photos/", "");
                            String sharedByLogoUrl = cleanLogo.startsWith("http")
                                    ? cleanLogo
                                    : "http://localhost:8070/api/v1/users/downloadFile/" + cleanLogo;
                            postDTO.setSharedByLogo(sharedByLogoUrl);
                        } else {
                            postDTO.setSharedByLogo(null);
                        }
                        if (lastShare.getDateShare() != null) {
                            // Convertir Instant → LocalDateTime selon le fuseau horaire du serveur
                            LocalDateTime sharedDate = LocalDateTime.ofInstant(
                                    lastShare.getDateShare().toInstant(),
                                    ZoneId.systemDefault()
                            );
                            postDTO.setSharedDate(sharedDate);
                        }





                        postDTO.setAdditionalContent(lastShare.getAdditionalContent());
                        postDTO.setSharedByRole(shareRole);
                    }

 return postDTO;
                })
                .collect(Collectors.toList());
    }
}
