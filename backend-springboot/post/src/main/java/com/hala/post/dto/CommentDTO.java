package com.hala.post.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CommentDTO {
        private Integer id;
        private String content;
        private LocalDateTime dateComment;
        private Integer userId;
        private Integer postId;
}
