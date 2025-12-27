package com.hala.post.Iservice;
import com.hala.post.dto.LikeDTO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface IServiceLike {
    LikeDTO likePost(Integer userId, Integer postId);
    void unlikePost(Integer likeId);
    List<LikeDTO> getAllLikes();
}
