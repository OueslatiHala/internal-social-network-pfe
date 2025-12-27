package com.hala.post.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventDTO {
    private Long id;
    private String title;
    private String company;
    private String date;
    private String imageUrl;
    private String description;
}
