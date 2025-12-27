package com.hala.post.mappers;

import com.hala.post.dto.ShareDTO;
import com.hala.post.entities.Share;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface ShareMapper {

    ShareMapper INSTANCE = Mappers.getMapper(ShareMapper.class);

    @Mapping(source = "post.id", target = "postId")
    @Mapping(source = "userId", target = "userId")
    @Mapping(source = "dateShare", target = "dateShare") // S’assure que le champ passe
    ShareDTO shareToShareDto(Share share);

    @Mapping(source = "postId", target = "post.id")
    @Mapping(source = "userId", target = "userId")
    @Mapping(source = "dateShare", target = "dateShare")
    Share shareDtoToShare(ShareDTO shareDTO);
}


