package com.hala.post.repository;
import com.hala.post.entities.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post,Integer> {
    Page<Post> findByUserId(Integer userId, Pageable pageable);
    @Query("SELECT p FROM Post p ORDER BY size(p.likes) DESC")
    List<Post> findTopLikedPosts(Pageable pageable);

    long countByUserIdIn(List<Integer> userIds);

}


