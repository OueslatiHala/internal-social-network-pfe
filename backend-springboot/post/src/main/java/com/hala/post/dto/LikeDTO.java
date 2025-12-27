package com.hala.post.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LikeDTO {
    private Integer id;
    private Integer postId;
    private Integer userId;
    private LocalDateTime dateLike;
}