package com.mbarca.ByR.service;

import com.mbarca.ByR.domain.ImageUrls;
import com.mbarca.ByR.dto.Response.PropertyPaginatedResponseDto;
import com.mbarca.ByR.dto.Response.PropertyResponseDto;
import com.mbarca.ByR.exceptions.NotFoundException;
import com.mbarca.ByR.exceptions.RepositoryException;
import com.mbarca.ByR.mapper.PropertyMapper;
import com.mbarca.ByR.model.Property;
import com.mbarca.ByR.model.PropertyImages;
import com.mbarca.ByR.repository.PropertyRepository;
import com.mbarca.ByR.utils.ImageCompressor;
import org.springframework.data.domain.Page;
import com.mbarca.ByR.utils.UrlGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.PageRequest;


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

    public void findPropertyByName (String name) throws RepositoryException {
        Optional<Property> propertyOptional = propertyRepository.findByName(name);
        if(propertyOptional.isPresent()) {
            throw new RepositoryException("Ya existe una propiedad con ese nombre");
        }
    }

    public void deleteProperty(UUID propertyId, String propertyName) {
        propertyRepository.deleteById(propertyId);
        fileStorageService.deletePropertyDirectory(propertyName);
    }

    public List<PropertyResponseDto> getFeaturedProperties() {
        List<Property> properties = propertyRepository.findFeaturedPropertiesWithImages();
        for(Property property : properties) {
            PropertyImages newImages = new PropertyImages();
            newImages.setUrl(urlGenerator.generateUrlList(property.getImages().get(property.getImageOrder().getFirst()).getUrl()));
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
            newImages.setThumbnailUrl(urlGenerator.generateUrlList(property.getImages().get(property.getImageOrder().getFirst()).getThumbnailUrl()));
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
            newImages.setThumbnailUrl(urlGenerator.generateUrlList(property.getImages().get(property.getImageOrder().getFirst()).getThumbnailUrl()));
            List<PropertyImages> newPropertyImages = new ArrayList<>();
            newPropertyImages.add(newImages);
            property.setImages(newPropertyImages);
        }
        return properties.stream().map(PropertyMapper.INSTANCE::toDto).toList();
    }

    public Page<PropertyResponseDto> getPaginatedProperties(int offset, int limit, String type, String category, String location) {
        Pageable pageable = PageRequest.of(offset, limit);
        String processedType = (type == null || type.trim().isEmpty()) ? null : type;
        String processedCategory = (category == null || category.trim().isEmpty()) ? null : category;
        String processedLocation = (location == null || location.trim().isEmpty()) ? null : location;


        Page<Property> propertyPage = propertyRepository.findAllWithFilters(processedType, processedCategory, processedLocation, pageable);

        return propertyPage.map(property -> {
            PropertyImages newImages = new PropertyImages();
            newImages.setThumbnailUrl(urlGenerator.generateUrlList(
                    property.getImages().get(property.getImageOrder().getFirst()).getThumbnailUrl())
            );

            List<PropertyImages> newPropertyImages = new ArrayList<>();
            newPropertyImages.add(newImages);
            property.setImages(newPropertyImages);

            return PropertyMapper.INSTANCE.toDto(property);
        });
    }

    public Property getById (UUID propertyId) {
        Optional<Property> propertyOptional = propertyRepository.findById(propertyId);
        Property property = new Property();
        if(propertyOptional.isPresent()) {
            property = propertyOptional.get();
        } else {
            throw new NotFoundException("Propiedad no encontrada");
        }
        List<PropertyImages> images = property.getImages();
        for(PropertyImages image : images) {
            image.setThumbnailUrl(urlGenerator.generateUrlList(image.getThumbnailUrl()));
            image.setUrl(urlGenerator.generateUrlList(image.getUrl()));
        }
        property.setImages(images);
        return property;
    }

    public Property getByIdToEdit (UUID propertyId) {
        Optional<Property> propertyOptional = propertyRepository.findById(propertyId);
        Property property = new Property();
        if(propertyOptional.isPresent()) {
            property = propertyOptional.get();
        } else {
            throw new NotFoundException("Propiedad no encontrada");
        }
        return property;
    }
}
