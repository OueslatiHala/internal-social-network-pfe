package com.hala.messagerie.Iservice;

import com.hala.messagerie.dto.MessageDTO;
import com.hala.messagerie.entities.Message;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
@Service
public interface IServiceMessage {
    MessageDTO envoyer_msg(MessageDTO messageDTO);
    List<MessageDTO> recevoir_msg(Integer userId);
    void supprimer_msg(Integer messageId);
    List<MessageDTO> consulter_msg();
}