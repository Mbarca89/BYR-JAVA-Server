package com.mbarca.ByR.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mbarca.ByR.domain.Images;
import com.mbarca.ByR.dto.Response.PropertyListResponseDto;
import com.mbarca.ByR.dto.Response.PropertyResponseDto;
import com.mbarca.ByR.mapper.PropertyMapper;
import com.mbarca.ByR.model.Property;
import com.mbarca.ByR.model.PropertyImages;
import com.mbarca.ByR.service.PropertyService;
import com.mbarca.ByR.utils.ImageCompressor;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/properties")
@CrossOrigin
public class PropertyController {

    @Autowired
    ImageCompressor imageCompressor;
    @Autowired
    private PropertyService propertyService;

    @PostMapping("/publish")
    public ResponseEntity<?> publishProperty(@RequestParam(value = "images", required = false) List<MultipartFile> files,
                                             @Valid @RequestParam("propertyData") String propertyJson) throws Exception {
        Property property = new ObjectMapper().readValue(propertyJson, Property.class);
        List<PropertyImages> propertyImagesList = new ArrayList<>();
        if (files != null && !files.isEmpty()) {
            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    Images compressedImages = imageCompressor.compressImage(file.getBytes(), true,file.getOriginalFilename(), property.getName());
                    PropertyImages propertyImage = new PropertyImages();
                    propertyImage.setThumbnailUrl(compressedImages.getPaths().get(1));
                    propertyImage.setUrl(compressedImages.getPaths().get(0));
                    propertyImage.setProperty(property);
                    propertyImagesList.add(propertyImage);
                }
            }
        }

        property.setImages(propertyImagesList);

        propertyService.publishProperty(property);

        return ResponseEntity.status(HttpStatus.OK).body("Propiedad publicada correctamente");
    }

    @GetMapping("/getPropertyList")
    public ResponseEntity<?> getPropertyList() {
        List<Property> properties = propertyService.getPropertyList();
        List<PropertyListResponseDto> propertyList = properties.stream().map(PropertyMapper.INSTANCE::toListDto).toList();
        return ResponseEntity.status(HttpStatus.OK).body(propertyList);
    }

    @DeleteMapping("/deleteProperty")
    public ResponseEntity<?> deleteProperty(@RequestParam UUID propertyId, @RequestParam String propertyName) {
        propertyService.deleteProperty(propertyId, propertyName);
        return ResponseEntity.status(HttpStatus.OK).body("Propiedad eliminada correctamente");
    }

    @GetMapping("/featured")
    public ResponseEntity<?> getFeaturedProperties () {
        List<PropertyResponseDto> properties = propertyService.getFeaturedProperties();
        return ResponseEntity.status(HttpStatus.OK).body(properties);
    }

    @GetMapping("/last")
    public ResponseEntity<?> getLastProperties () {
        List<PropertyResponseDto> properties = propertyService.getLastProperties();
        return ResponseEntity.status(HttpStatus.OK).body(properties);
    }

    @GetMapping("")
    public ResponseEntity<?> getAllProperties () {
        List<PropertyResponseDto> properties = propertyService.getAllProperties();
        return ResponseEntity.status(HttpStatus.OK).body(properties);
    }

    @GetMapping("/getById")
    public ResponseEntity<?> getAllProperties (@RequestParam UUID propertyId) {
        PropertyResponseDto property = propertyService.getById(propertyId);
        return ResponseEntity.status(HttpStatus.OK).body(property);
    }

}
