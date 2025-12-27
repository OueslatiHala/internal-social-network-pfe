package com.hala.post.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
@Builder
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String company;
    private String date;

    @Column(name = "image_url")
    private String imageUrl;


    @Column(columnDefinition = "TEXT") // ✅ aussi pour les longues descriptions
    private String description;
}
