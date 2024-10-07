package com.mbarca.ByR.service;

import com.mbarca.ByR.domain.ImageUrls;
import com.mbarca.ByR.dto.Response.PropertyResponseDto;
import com.mbarca.ByR.exceptions.NotFoundException;
import com.mbarca.ByR.mapper.PropertyMapper;
import com.mbarca.ByR.model.Property;
import com.mbarca.ByR.model.PropertyImages;
import com.mbarca.ByR.repository.PropertyRepository;
import com.mbarca.ByR.utils.ImageCompressor;
import com.mbarca.ByR.utils.UrlGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PropertyService {
    @Autowired
    PropertyRepository propertyRepository;

    @Autowired
    FileStorageService fileStorageService;

    @Autowired
    UrlGenerator urlGenerator;

    public void publishProperty(Property property) {
            propertyRepository.save(property);
    }

    public List<Property> getPropertyList() {
        return propertyRepository.findAll();
    }

    public void deleteProperty(UUID propertyId, String propertyName) {
        propertyRepository.deleteById(propertyId);
        fileStorageService.deletePropertyDirectory(propertyName);
    }

    public List<PropertyResponseDto> getFeaturedProperties() {
        List<Property> properties = propertyRepository.findFeaturedPropertiesWithImages();
        for(Property property : properties) {
            PropertyImages newImages = new PropertyImages();
            newImages.setUrl(urlGenerator.generateUrlList(property.getImages().getFirst().getUrl()));
            List<PropertyImages> newPropertyImages = new ArrayList<>();
            newPropertyImages.add(newImages);
            property.setImages(newPropertyImages);
        }
        return properties.stream().map(PropertyMapper.INSTANCE::toDto).toList();
    }

    public List<PropertyResponseDto> getLastProperties() {
        List<Property> properties = propertyRepository.findTop10ByOrderByCreatedAtDesc();
        for(Property property : properties) {
            PropertyImages newImages = new PropertyImages();
            newImages.setThumbnailUrl(urlGenerator.generateUrlList(property.getImages().getFirst().getThumbnailUrl()));
            List<PropertyImages> newPropertyImages = new ArrayList<>();
            newPropertyImages.add(newImages);
            property.setImages(newPropertyImages);
        }
        return properties.stream().map(PropertyMapper.INSTANCE::toDto).toList();
    }

    public List<PropertyResponseDto> getAllProperties() {
        List<Property> properties = propertyRepository.findAll();
        for(Property property : properties) {
            PropertyImages newImages = new PropertyImages();
            newImages.setThumbnailUrl(urlGenerator.generateUrlList(property.getImages().getFirst().getThumbnailUrl()));
            List<PropertyImages> newPropertyImages = new ArrayList<>();
            newPropertyImages.add(newImages);
            property.setImages(newPropertyImages);
        }
        return properties.stream().map(PropertyMapper.INSTANCE::toDto).toList();
    }

    public PropertyResponseDto getById (UUID propertyId) {
        Optional<Property> propertyOptional = propertyRepository.findById(propertyId);
        Property property = new Property();
        if(propertyOptional.isPresent()) {
            property = propertyOptional.get();
        } else {
            throw new NotFoundException("Propiedad no encontrada");
        }
        List<PropertyImages> images = property.getImages();
        for(PropertyImages image : images) {
            image.setUrl(urlGenerator.generateUrlList(image.getUrl()));
        }
        property.setImages(images);
        return PropertyMapper.INSTANCE.toDto(property);
    }
}
