package com.hala.post.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Date;

@Data
@Entity
@Table(name = "shares")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Share {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @Column(name = "additional_content")
    private String additionalContent;

    @Column(name = "date_share", nullable = false)
    private Date dateShare;

}