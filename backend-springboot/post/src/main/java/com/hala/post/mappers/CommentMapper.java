package com.hala.post.mappers;

import com.hala.post.dto.CommentDTO;
import com.hala.post.entities.Comment;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CommentMapper {
    CommentDTO commentToCommentDto(Comment comment);
    Comment commentDtoToComment(CommentDTO commentDTO);
}