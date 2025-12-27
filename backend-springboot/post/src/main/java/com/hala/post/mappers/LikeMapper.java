package com.hala.post.mappers;

import com.hala.post.dto.LikeDTO;
import com.hala.post.entities.Like;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface LikeMapper {
    @Mapping(source = "post.id", target = "postId")
    LikeDTO likeToLikeDto(Like like);

    @Mapping(source = "postId", target = "post.id")
    Like likeDtoToLike(LikeDTO likeDto);
}