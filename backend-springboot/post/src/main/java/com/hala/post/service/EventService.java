package com.hala.post.service;

import com.hala.post.dto.EventDTO;
import com.hala.post.entities.Event;
import com.hala.post.mappers.EventMapper;
import com.hala.post.repository.EventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class EventService {

    @Autowired
    private EventRepository repository;

    @Autowired
    private EventMapper eventMapper;

    public List<EventDTO> getAll() {
        return repository.findAll().stream()
                .map(eventMapper::eventToDTO)
                .collect(Collectors.toList());
    }
    public void deleteEvent(Long id) {
        repository.deleteById(id);
    }
    public EventDTO updateEvent(Long id, EventDTO dto) {
        Event event = repository.findById(id).orElseThrow();
        event.setTitle(dto.getTitle());
        event.setCompany(dto.getCompany());
        event.setDate(dto.getDate());
        event.setDescription(dto.getDescription());
        if (dto.getImageUrl() != null) {
            event.setImageUrl(dto.getImageUrl());
        }
        return eventMapper.eventToDTO(repository.save(event));
    }

    public EventDTO create(EventDTO dto) {
        Event event = eventMapper.dtoToEvent(dto);
        return eventMapper.eventToDTO(repository.save(event));
    }
}
