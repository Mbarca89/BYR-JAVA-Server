package com.mbarca.ByR.controller;
import com.mbarca.ByR.service.*;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;
@RestController @RequestMapping("/api/images")
public class ImageController {
    private final FileStorageService storage;
    private final ImageService images;
    public ImageController(FileStorageService storage, ImageService images) { this.storage = storage; this.images = images; }
    @GetMapping("/{property}/{filename:.+}")
    public ResponseEntity<Resource> getImage(@PathVariable String property, @PathVariable String filename) {
        return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(storage.loadImage(property, filename));
    }
    @DeleteMapping("/delete") public ResponseEntity<String> deleteById(@RequestParam UUID id) {
        return ResponseEntity.ok(images.deleteImage(id));
    }
}
