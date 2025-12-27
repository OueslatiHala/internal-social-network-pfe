package com.hala.post.mappers;

import com.hala.post.dto.EventDTO;
import com.hala.post.entities.Event;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface EventMapper {

    EventDTO eventToDTO(Event event);

    Event dtoToEvent(EventDTO dto);
}
