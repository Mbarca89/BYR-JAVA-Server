package com.mbarca.ByR.service;

import com.mbarca.ByR.exceptions.NotFoundException;
import com.mbarca.ByR.model.Property;
import com.mbarca.ByR.model.PropertyImages;
import com.mbarca.ByR.repository.ImageRepository;
import com.mbarca.ByR.repository.PropertyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ImageService {
    @Autowired
    ImageRepository imageRepository;

    @Autowired
    PropertyRepository propertyRepository;

    @Autowired
    FileStorageService fileStorageService;

    public String deleteImage (UUID id) {
        PropertyImages deleteImage = imageRepository.findById(id).orElseThrow(() -> new NotFoundException("Imagen no encontrada"));
        Property property = propertyRepository.findById(deleteImage.getProperty().getId()).orElseThrow(() -> new NotFoundException("Propiedad no encontrada"));
        List<PropertyImages> images = property.getImages();
        images.removeIf(image -> image.getId().equals(id));
        property.setImages(images);
        propertyRepository.save(property);
        fileStorageService.deleteFileByPath(deleteImage.getUrl());
        fileStorageService.deleteFileByPath(deleteImage.getThumbnailUrl());
        return "Imagen eliminada correctamente";
    }
}
