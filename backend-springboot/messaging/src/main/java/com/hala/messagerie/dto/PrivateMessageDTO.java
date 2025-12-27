package com.hala.messagerie.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.hala.messagerie.client.UserDTO;
import com.hala.messagerie.entities.PrivateMessage;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PrivateMessageDTO {
    private Integer id;



    @JsonProperty("content")
    private String contenu;

    private String senderName;
    private String senderPicture;

    private Date dateMsg;
    private Integer senderId;
    private Integer recipientId;
    private boolean consulter;


    public static PrivateMessageDTO fromEntity(PrivateMessage msg, UserDTO sender) {
        PrivateMessageDTO dto = new PrivateMessageDTO();
        dto.setId(msg.getId());
        dto.setContenu(msg.getContenu()); // ou dto.setContent si tu l’as renommé
        dto.setDateMsg(msg.getDateMsg());
        dto.setSenderId(msg.getSenderId());
        dto.setRecipientId(msg.getRecipientId());
        dto.setConsulter(msg.isConsulter());

        // 💡 👉 C’est ICI que tu enrichis avec les infos du sender :
        if (sender != null) {
            if ("PARTENAIRE".equalsIgnoreCase(sender.getRole().name())) {
                dto.setSenderName(sender.getCompanyName());
            } else {
                dto.setSenderName(sender.getFirstname() + " " + sender.getLastname());
            }

            dto.setSenderPicture(sender.getProfilePicture() != null ? sender.getProfilePicture() : sender.getLogo());
        }


        return dto;
    }


}


