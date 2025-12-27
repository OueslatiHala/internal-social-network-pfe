package com.hala.post.repository;

import com.hala.post.entities.Event;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findAllByOrderByDateDesc(); // ← nom exact du champ dans l'entité
}
