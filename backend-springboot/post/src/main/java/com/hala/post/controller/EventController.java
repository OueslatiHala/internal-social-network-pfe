package com.hala.post.controller;

import com.hala.post.dto.EventDTO;
import com.hala.post.service.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/events")
@CrossOrigin
public class EventController {

    @Autowired
    private EventService service;

    @GetMapping
    public List<EventDTO> getAllEvents() {
        return service.getAll();
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEvent(@PathVariable Long id) {
        service.deleteEvent(id);
        return ResponseEntity.noContent().build();
    }
    @PutMapping("/{id}")
    public ResponseEntity<EventDTO> updateEvent(
            @PathVariable Long id,
            @RequestPart("event") EventDTO dto,
            @RequestPart(value = "image", required = false) MultipartFile image
    ) {
        if (image != null && !image.isEmpty()) {
            try {
                String fileName = UUID.randomUUID() + "_" + image.getOriginalFilename();
                Path path = Paths.get(System.getProperty("user.dir"), "file_storage", fileName);
                Files.copy(image.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
                dto.setImageUrl(fileName);
            } catch (IOException e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        }

        return ResponseEntity.ok(service.updateEvent(id, dto));
    }

    @PostMapping
    public ResponseEntity<EventDTO> createEvent(
            @RequestPart("event") EventDTO dto,
            @RequestPart(value = "image", required = false) MultipartFile image
    ) {
        // 1. Sauvegarder l’image si elle est présente
        if (image != null && !image.isEmpty()) {
            try {
                String fileName = UUID.randomUUID() + "_" + image.getOriginalFilename();
                Path path = Paths.get(System.getProperty("user.dir"), "file_storage", fileName);
                Files.copy(image.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

                // 2. Mettre à jour le champ imageUrl dans le DTO
                dto.setImageUrl(fileName); // IMPORTANT : assure-toi que EventDTO a un champ `imageUrl`
            } catch (IOException e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        }

        // 3. Sauvegarder l'événement avec imageUrl
        return new ResponseEntity<>(service.create(dto), HttpStatus.CREATED);
    }


    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> uploadEventImage(@RequestParam("file") MultipartFile file) {
        try {
            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path path = Paths.get(System.getProperty("user.dir"), "file_storage", fileName);
            Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

            Map<String, String> response = new HashMap<>();
            response.put("url", fileName);
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/files/{filename:.+}")
    public ResponseEntity<Resource> getEventImage(@PathVariable String filename) {
        try {
            Path file = Paths.get(System.getProperty("user.dir"), "file_storage").resolve(filename).normalize();
            Resource resource = new UrlResource(file.toUri());

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}
