package com.hala.post.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDateTime;

@Getter
@Setter
public class PostWithUserDetailsDTO {
    private Integer id;
    private String content;
    private LocalDateTime sharedDate;
    private String url;
    private Integer userId;
    private String userFirstname;
    private String userLastname;
    private String companyName;
    private String role;
    private String profilePicture;
    private String logo;
    private LocalDateTime datePub;

    // Pour les partages, tu peux ajouter :
    private String sharedByFirstname;
    private String sharedByLastname;
    private String sharedByCompanyName;
    private String sharedByRole;
    private String sharedByProfilePicture;
    private String sharedByLogo;
    private String additionalContent;
    private int likesCount;
    private int commentsCount;
    private int shareCount;

}
