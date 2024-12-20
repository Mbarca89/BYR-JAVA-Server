package com.mbarca.ByR.controller;

import com.mbarca.ByR.service.ImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@RestController
@RequestMapping("/api/images")
@CrossOrigin
public class ImageController {

    @Value("${file.storage.location}")
    private String storageLocation;

    @Autowired
    ImageService imageService;

    @GetMapping("/{property}/{filename:.+}")
    public ResponseEntity<Resource> getImage(
            @PathVariable String property,
            @PathVariable String filename) {
        try {
            // Construir la ruta completa del archivo
            Path filePath = Paths.get(storageLocation)
                    .resolve(property)
                    .resolve(filename)
                    .normalize();

            // Crear el recurso
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() && resource.isReadable()) {
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                        .body(resource);
            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        } catch (MalformedURLException e) {
            return ResponseEntity.badRequest().body(null);
        } catch (Exception e) {
            e.printStackTrace();  // Agregar log para detalles del error
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteById (@RequestParam UUID id) {
        String response = imageService.deleteImage(id);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

}
