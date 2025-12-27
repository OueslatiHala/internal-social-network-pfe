package com.hala.messagerie.mappers;

import com.hala.messagerie.dto.PrivateMessageDTO;
import com.hala.messagerie.entities.PrivateMessage;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface PrivateMessageMapper {
    PrivateMessageDTO privateMessageToPrivateMessageDto(PrivateMessage privateMessage);
    PrivateMessage privateMessageDtoToPrivateMessage(PrivateMessageDTO dto);
    void updatePrivateMessageFromDto(PrivateMessageDTO dto, @MappingTarget PrivateMessage privateMessage);
}

