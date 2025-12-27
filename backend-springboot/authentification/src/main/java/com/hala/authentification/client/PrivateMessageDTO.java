package com.hala.authentification.client;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PrivateMessageDTO {
    private Integer id;
    private String contenu;
    private Date date_msg;
    private String destinataire;
    private String source;
    private Integer userId;
}
