package com.hala.post.controller;

import com.hala.post.client.AuthClient;
import com.hala.post.client.UserDTO;
import com.hala.post.dto.ShareDTO;
import com.hala.post.entities.Share;
import com.hala.post.mappers.ShareMapper;
import com.hala.post.repository.ShareRepository;
import com.hala.post.service.ShareService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/shares")
public class ShareController {

    private final ShareService shareService;
    private final ShareRepository shareRepository;
    private final ShareMapper shareMapper;
    private final AuthClient authClient;



    @Autowired
    public ShareController(ShareService shareService, ShareRepository shareRepository, ShareMapper shareMapper, AuthClient authClient) {
        this.shareService = shareService;
        this.shareRepository = shareRepository;

        this.shareMapper = shareMapper;
        this.authClient = authClient;
    }

    @PostMapping("/share")
    public ShareDTO sharePost(
            @RequestParam Integer userId,
            @RequestParam Integer postId,
            @RequestParam(required = false) String additionalContent) {
        return shareService.sharePost(userId, postId, additionalContent);
    }


    @GetMapping("/byPost/{postId}")
    public List<ShareDTO> getSharesByPostId(@PathVariable Integer postId) {
        List<Share> shares = shareRepository.findByPostId(postId);
        return shares.stream().map(share -> {
            ShareDTO dto = shareMapper.shareToShareDto(share);
            // C'EST ICI QUE ÇA SE JOUE :
            dto.setUser(authClient.getUserById(share.getUserId()));
            return dto;
        }).toList();
    }
    @DeleteMapping("/{shareId}")
    public ResponseEntity<Void> deleteShare(@PathVariable Integer shareId) {
        shareService.deleteShare(shareId);
        return ResponseEntity.noContent().build();
    }
    @GetMapping("/user/{userId}")
    public List<ShareDTO> getSharesByUser(@PathVariable Integer userId) {
        List<Share> shares = shareRepository.findByUserId(userId);
        return shares.stream().map(share -> {
            ShareDTO dto = shareMapper.shareToShareDto(share);
            dto.setUser(authClient.getUserById(share.getUserId()));
            return dto;
        }).toList();
    }


    @GetMapping("/byPost/{postId}/users")
    public List<UserDTO> getSharedUsersForPost(@PathVariable Integer postId) {
        return shareService.getSharedUsersForPost(postId);
    }

    @GetMapping
    public List<ShareDTO> getAllShares() {
        return shareService.getAllShares();
    }
}
