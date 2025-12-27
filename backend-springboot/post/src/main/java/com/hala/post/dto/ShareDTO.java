package com.hala.post.dto;
import com.hala.post.client.UserDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Date;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShareDTO {
    private Integer id;
    private Instant dateShare;

    private Integer userId;
    private Integer postId;
    private UserDTO user;
    private String additionalContent; // ajoute-le si besoin côté front
}
