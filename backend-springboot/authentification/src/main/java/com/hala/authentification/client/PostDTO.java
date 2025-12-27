package com.hala.authentification.client;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostDTO {
    private Integer id;
    private String content;
    private Date datePub;
    private String url;
    private Integer userId;

}
