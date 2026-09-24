package com.mbarca.ByR.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mbarca.ByR.domain.Images;
import com.mbarca.ByR.dto.Response.PropertyListResponseDto;
import com.mbarca.ByR.dto.Response.PropertyPaginatedResponseDto;
import com.mbarca.ByR.dto.Response.PropertyResponseDto;
import com.mbarca.ByR.mapper.PropertyMapper;
import com.mbarca.ByR.model.Property;
import com.mbarca.ByR.model.PropertyImages;
import com.mbarca.ByR.service.PropertyService;
import com.mbarca.ByR.utils.ImageCompressor;
import jakarta.validation.Valid;
import jakarta.validation.Validator;
import jakarta.validation.ConstraintViolationException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/properties")

public class PropertyController {
    @Autowired private Validator validator;
    @Autowired private ObjectMapper objectMapper;

    private Property parseProperty(String json) throws Exception {
        Property property = objectMapper.readValue(json, Property.class);
        if (property.getName() != null) property.setName(property.getName().trim());
        var violations = validator.validate(property);
        if (!violations.isEmpty()) throw new ConstraintViolationException(violations);
        return property;
    }

    @Autowired
    ImageCompressor imageCompressor;
    @Autowired
    private PropertyService propertyService;

    @PostMapping("/publish")
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<?> publishProperty(@RequestParam(value = "images", required = false) List<MultipartFile> files,
                                             @Valid @RequestParam("propertyData") String propertyJson) throws Exception {
        Property property = parseProperty(propertyJson);
        property.setId(null);
        String imageDirectory = UUID.randomUUID().toString();
        propertyService.findPropertyByName(property.getName());
        List<PropertyImages> propertyImagesList = new ArrayList<>();
        if (files != null && !files.isEmpty()) {
            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    Images compressedImages = imageCompressor.compressImage(file.getBytes(), true, file.getOriginalFilename(), imageDirectory);
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
    public ResponseEntity<?> deleteProperty(@RequestParam UUID propertyId) {
        propertyService.deleteProperty(propertyId);
        return ResponseEntity.status(HttpStatus.OK).body("Propiedad eliminada correctamente");
    }

    @GetMapping("/featured")
    public ResponseEntity<?> getFeaturedProperties() {
        List<PropertyResponseDto> properties = propertyService.getFeaturedProperties();
        return ResponseEntity.status(HttpStatus.OK).body(properties);
    }

    @GetMapping("/last")
    public ResponseEntity<?> getLastProperties() {
        List<PropertyResponseDto> properties = propertyService.getLastProperties();
        return ResponseEntity.status(HttpStatus.OK).body(properties);
    }

    @GetMapping("")
    public ResponseEntity<?> getAllProperties() {
        List<PropertyResponseDto> properties = propertyService.getAllProperties();
        return ResponseEntity.status(HttpStatus.OK).body(properties);
    }

    @GetMapping("/paginated")
    public ResponseEntity<?> getPaginatedProperties(@RequestParam int limit,
                                                    @RequestParam int offset,
                                                    @RequestParam(required = false) String type,
                                                    @RequestParam(required = false) String category,
                                                    @RequestParam(required = false) String location,
                                                    PagedResourcesAssembler<PropertyResponseDto> assembler) {
        Page<PropertyResponseDto> properties = propertyService.getPaginatedProperties(offset, limit, type, category, location);
        return ResponseEntity.status(HttpStatus.OK).body(assembler.toModel(properties));
    }

    @GetMapping("/getById")
    public ResponseEntity<?> getPropertyById(@RequestParam UUID propertyId) {
        PropertyResponseDto response = propertyService.getById(propertyId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PutMapping("/edit/{id}")
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<?> editProperty(@PathVariable("id") UUID propertyId,
                                          @RequestParam(value = "images", required = false) List<MultipartFile> files,
                                          @Valid @RequestParam("propertyData") String propertyJson) throws Exception {
        Property updatedProperty = parseProperty(propertyJson);
        propertyService.assertUniqueName(updatedProperty.getName(), propertyId);
        Property existingProperty = propertyService.getByIdToEdit(propertyId);

        existingProperty.setName(updatedProperty.getName());
        existingProperty.setDescription(updatedProperty.getDescription());
        existingProperty.setType(updatedProperty.getType());
        existingProperty.setCategory(updatedProperty.getCategory());
        existingProperty.setPrice(updatedProperty.getPrice());
        existingProperty.setCurrency(updatedProperty.getCurrency());
        existingProperty.setLocation(updatedProperty.getLocation());
        existingProperty.setSize(updatedProperty.getSize());
        existingProperty.setConstructed(updatedProperty.getConstructed());
        existingProperty.setBedrooms(updatedProperty.getBedrooms());
        existingProperty.setBathrooms(updatedProperty.getBathrooms());
        existingProperty.setKitchen(updatedProperty.getKitchen());
        existingProperty.setGarage(updatedProperty.getGarage());
        existingProperty.setOthers(updatedProperty.getOthers());
        existingProperty.setServices(updatedProperty.getServices());
        existingProperty.setAmenities(updatedProperty.getAmenities());
        existingProperty.setFeatured(updatedProperty.getFeatured());
        existingProperty.setImageOrder(updatedProperty.getImageOrder());

        if (files != null && !files.isEmpty()) {
            List<PropertyImages> newPropertyImages = new ArrayList<>();
            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    Images compressedImages = imageCompressor.compressImage(file.getBytes(), true, file.getOriginalFilename(), propertyId.toString());
                    PropertyImages propertyImage = new PropertyImages();
                    propertyImage.setThumbnailUrl(compressedImages.getPaths().get(1));
                    propertyImage.setUrl(compressedImages.getPaths().get(0));
                    propertyImage.setProperty(existingProperty);
                    newPropertyImages.add(propertyImage);
                }
            }
            existingProperty.getImages().addAll(newPropertyImages);
        }

        propertyService.publishProperty(existingProperty);

        return ResponseEntity.status(HttpStatus.OK).body("Propiedad editada correctamente");
    }

}
