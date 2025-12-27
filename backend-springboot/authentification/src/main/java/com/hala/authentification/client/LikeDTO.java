package com.hala.authentification.client;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LikeDTO {
    private Integer id;
    private Date date_like;
    private Integer userId;
    private Integer postId;
}



