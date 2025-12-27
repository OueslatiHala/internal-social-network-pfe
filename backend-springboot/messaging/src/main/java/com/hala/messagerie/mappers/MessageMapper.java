package com.hala.messagerie.mappers;

import com.hala.messagerie.dto.MessageDTO;
import com.hala.messagerie.entities.Message;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
@Mapper(componentModel = "spring")
public interface MessageMapper {
    MessageMapper INSTANCE = Mappers.getMapper(MessageMapper.class);

    MessageDTO likeToDto(Message like);
    Message likeDtoToLike(MessageDTO likeDTO);
}

