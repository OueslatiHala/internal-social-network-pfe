package com.hala.post.repository;

import com.hala.post.entities.Like;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface LikeRepository extends JpaRepository<Like, Integer> {
    List<Like> findByPostId(Integer postId);
    Optional<Like> findByUserIdAndPostId(Integer userId, Integer postId);
    boolean existsByUserIdAndPostId(Integer userId, Integer postId);
    Long countByPostId(Integer postId);
    void deleteById(Integer id);
    List<Like> findAllByPostId(Integer postId);
}