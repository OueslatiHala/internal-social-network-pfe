package com.hala.authentification.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class StorageService {

    @Value("${app.profile-photos-path:profile-photos}")
    private String uploadDir;

    public Resource loadAsResource(String filename) {
        try {
            // On cherche le fichier dans le dossier profile-photos
            Path file = Paths.get(uploadDir).resolve(filename).normalize();
            if (!Files.exists(file)) {
                throw new RuntimeException("File not found: " + file.toString());
            }
            Resource resource = new UrlResource(file.toUri());
            if(resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException("File not readable: " + file.toString());
            }
        } catch (Exception e) {
            throw new RuntimeException("Could not read file: " + filename, e);
        }
    }
}
