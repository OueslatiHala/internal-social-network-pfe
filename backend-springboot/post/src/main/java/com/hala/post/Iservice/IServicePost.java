package com.hala.post.Iservice;
import com.hala.post.dto.PostDTO;
import com.hala.post.entities.Post;
import org.springframework.stereotype.Service;

import java.util.List;



    @Service
    public interface IServicePost {
        PostDTO createPost(Integer userId, PostDTO postDTO);
        PostDTO updatePost(Integer postId, PostDTO postDTO);
        void deletePost(Integer postId);
        List<PostDTO> findAllPostsByUser(Integer userId);
        Post findPostById(Integer postId);

}
