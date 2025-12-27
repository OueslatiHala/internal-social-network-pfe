package com.hala.messagerie.repository;

import com.hala.messagerie.entities.PrivateMessage;
import feign.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PrivateMessageRepository extends JpaRepository<PrivateMessage, Integer> {
    List<PrivateMessage> findByRecipientId(Integer recipientId);
    List<PrivateMessage> findBySenderId(Integer senderId);
    @Query("SELECT DISTINCT CASE WHEN pm.senderId = :userId THEN pm.recipientId ELSE pm.senderId END " +
            "FROM PrivateMessage pm WHERE pm.senderId = :userId OR pm.recipientId = :userId")
    List<Integer> findConversationUserIds(@Param("userId") Integer userId);
    long countByRecipientIdAndConsulterFalse(Integer recipientId);
    @Modifying
    @Query("UPDATE PrivateMessage m SET m.consulter = true WHERE m.recipientId = :userId AND m.senderId = :otherUserId AND m.consulter = false")
    void markConversationAsRead(@Param("userId") Integer userId, @Param("otherUserId") Integer otherUserId);

    @Query("FROM PrivateMessage pm WHERE (pm.senderId = :userId1 AND pm.recipientId = :userId2) " +
            "OR (pm.senderId = :userId2 AND pm.recipientId = :userId1) ORDER BY pm.dateMsg ASC")
    List<PrivateMessage> findConversation(@Param("userId1") Integer userId1, @Param("userId2") Integer userId2);
    @Query("SELECT m FROM PrivateMessage m WHERE m.senderId = :userId OR m.recipientId = :userId")
    List<PrivateMessage> findAllMessagesForUser(@Param("userId") Integer userId);
    @Modifying
    @Query("DELETE FROM PrivateMessage m WHERE (m.senderId = :userId AND m.recipientId = :otherUserId) OR (m.senderId = :otherUserId AND m.recipientId = :userId)")
    void deleteConversationBetweenUsers(@Param("userId") Integer userId, @Param("otherUserId") Integer otherUserId);
    @Query("SELECT COUNT(pm) FROM PrivateMessage pm WHERE pm.senderId = :otherUserId AND pm.recipientId = :userId AND pm.consulter = false")
    Long countBySenderIdAndRecipientIdAndConsulterFalse(@Param("otherUserId") Integer otherUserId, @Param("userId") Integer userId);
    List<PrivateMessage> findByRecipientIdAndConsulterFalse(Integer recipientId);

}
