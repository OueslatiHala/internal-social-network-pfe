package com.hala.authentification.client;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShareDTO {

    private Integer id;
    private Date date_share;
    private Integer userId;
    private Integer postId;

    public void setPostId(Integer  postId) {
        this.postId = postId;
    }
}