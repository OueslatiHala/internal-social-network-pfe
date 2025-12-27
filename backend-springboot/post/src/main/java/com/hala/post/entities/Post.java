package com.hala.post.entities;

import com.hala.post.client.UserDTO;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "Post")
public class Post {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Integer id;

        private LocalDateTime datePub;
        private String url;
        private Integer userId;

        private String content;        // ⚠️ AJOUTE ce champ pour le contenu du post

        @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
        private List<Comment> comments;

        @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
        private List<Like> likes;

        @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
        private List<Share> shares;


}
