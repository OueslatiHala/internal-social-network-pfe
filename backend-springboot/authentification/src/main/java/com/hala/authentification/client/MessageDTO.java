package com.hala.authentification.client;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageDTO {
    private Integer id;
    private String contenu;
    private Date date_msg;
    private Integer userId;
    private Integer groupMessageId;


}
