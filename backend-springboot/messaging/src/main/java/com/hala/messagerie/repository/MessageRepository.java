package com.hala.messagerie.repository;

import com.hala.messagerie.entities.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message,Integer> {
    List<Message> findAllByUserId(Integer userId);
}
