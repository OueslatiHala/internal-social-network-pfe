package com.hala.post.mappers;

import com.hala.post.dto.PostDTO;
import com.hala.post.dto.PostWithUserDetailsDTO;
import com.hala.post.entities.Post;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")

public interface PostMapper {

        PostDTO postToPostDto(Post post);
        Post postDtoToPost(PostDTO Post);
    PostWithUserDetailsDTO postToPostWithUserDto(Post post);
    }

