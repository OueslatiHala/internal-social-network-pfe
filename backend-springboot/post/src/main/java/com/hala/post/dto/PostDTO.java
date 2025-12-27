package com.hala.post.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostDTO {
    private Integer id;
    private Date datePub;
    private String url;
    private Integer userId;

    private String content;       // <--- Ajoute le champ content !

    // Pour enrichir l'affichage :
    private String userFirstname;
    private String userLastname;
    private String companyName;
    private String profilePicture;
    private String logo;

    private List<CommentDTO> comments;
    private List<LikeDTO> likes;
    private List<ShareDTO> shares = new ArrayList<>();

}
