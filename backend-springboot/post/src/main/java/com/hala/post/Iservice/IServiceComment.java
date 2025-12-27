package com.hala.post.Iservice;

import com.hala.post.dto.CommentDTO;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public interface IServiceComment {
    CommentDTO addComment(Integer userId, Integer postId, String content);
    CommentDTO updateComment(Integer commentId, String content);
    void deleteComment(Integer commentId);
    List<CommentDTO> getAllComments();
}
