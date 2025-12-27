package com.hala.authentification.client;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentDTO {
    private Integer id;
    private String content;
    private Date date_comment;
    private Integer userId;
    private Integer postId;
    public void setPostId(Integer postId) {
        this.postId = postId;
    }
}

