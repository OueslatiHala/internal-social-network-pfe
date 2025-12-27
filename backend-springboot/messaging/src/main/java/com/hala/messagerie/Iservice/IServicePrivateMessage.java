package com.hala.messagerie.Iservice;

import com.hala.messagerie.dto.PrivateMessageDTO;
import java.util.List;

public interface IServicePrivateMessage {

    /**
     * Sends a private message.
     *
     * @param userId the ID of the user sending the message
     * @param privateMessageDTO the message details
     * @return the sent message details
     */
    PrivateMessageDTO sendMessage(Integer userId, PrivateMessageDTO privateMessageDTO);

    /**
     * Marks a message as received.
     *
     * @param messageId the ID of the message to be marked as received
     * @return the updated message details
     */
    PrivateMessageDTO receiveMessage(Integer messageId);

    /**
     * Deletes a private message.
     *
     * @param messageId the ID of the message to be deleted
     */
    void deleteMessage(Integer id);


    /**
     * Finds all messages for a specific user.
     *
     * @param userId the ID of the user
     * @return a list of messages for the user
     */
    List<PrivateMessageDTO> findAllMessagesByUser(Integer userId);

    /**
     * Finds a message by its ID.
     *
     * @param messageId the ID of the message
     * @return the message details
     */
    PrivateMessageDTO findMessageById(Integer messageId);
}
