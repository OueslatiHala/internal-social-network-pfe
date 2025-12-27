package com.hala.messagerie.service;

import com.hala.messagerie.Iservice.IServiceMessage;
import com.hala.messagerie.client.AuthClient;
import com.hala.messagerie.client.UserDTO;
import com.hala.messagerie.dto.MessageDTO;
import com.hala.messagerie.entities.Message;
import com.hala.messagerie.repository.MessageRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MessageService implements IServiceMessage {

    private final MessageRepository messageRepository;
    private final AuthClient authClient;

    @Autowired
    public MessageService(MessageRepository messageRepository, AuthClient authClient) {
        this.messageRepository = messageRepository;
        this.authClient = authClient;
    }

    @Override
    public MessageDTO envoyer_msg(MessageDTO messageDTO) {
        UserDTO userDTO = authClient.getUserById(messageDTO.getUserId());
        if (userDTO == null) {
            throw new EntityNotFoundException("User not found with id: " + messageDTO.getUserId());
        }

        Message message = new Message();
        message.setContenu(messageDTO.getContenu());
        message.setDate_msg(new Date());
        message.setUserId(messageDTO.getUserId());
        Message savedMessage = messageRepository.save(message);
        return new MessageDTO(savedMessage);
    }

    @Override
    public List<MessageDTO> recevoir_msg(Integer userId) {
        List<Message> messages = messageRepository.findAllByUserId(userId);
        return messages.stream()
                .map(MessageDTO::new)
                .collect(Collectors.toList());
    }

    @Override
    public void supprimer_msg(Integer messageId) {
        messageRepository.deleteById(messageId);
    }

    @Override
    public List<MessageDTO> consulter_msg() {
        List<Message> messages = messageRepository.findAll();
        return messages.stream()
                .map(MessageDTO::new)
                .collect(Collectors.toList());
    }
}
