package com.hala.messagerie.dto;

import com.hala.messagerie.entities.Message;
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

    public MessageDTO(Message message) {
        this.id = message.getId();
        this.contenu = message.getContenu();
        this.date_msg = message.getDate_msg();
        this.userId = message.getUserId();
    }
}
