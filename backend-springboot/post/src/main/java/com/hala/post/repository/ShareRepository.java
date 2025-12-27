package com.hala.post.repository;

import com.hala.post.entities.Share;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShareRepository extends JpaRepository<Share, Integer> {

    List<Share> findAllByPostId(Integer postId);
    List<Share> findByUserId(Integer userId);
    Page<Share> findByUserId(Integer userId, Pageable pageable);

    List<Share> findAllByUserId(Integer userId);
    List<Share> findByPostId(Integer postId);
    void deleteAllByPost_Id(Integer postId);
}
