package com.mbarca.ByR.service;

import com.mbarca.ByR.dto.Response.PropertyResponseDto;
import com.mbarca.ByR.exceptions.NotFoundException;
import com.mbarca.ByR.exceptions.RepositoryException;
import com.mbarca.ByR.mapper.PropertyMapper;
import com.mbarca.ByR.model.*;
import com.mbarca.ByR.repository.PropertyRepository;
import com.mbarca.ByR.utils.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service
public class PropertyService {
    @Autowired PropertyRepository propertyRepository;
    @Autowired FileStorageService fileStorageService;
    @Autowired UrlGenerator urlGenerator;

    @Transactional
    public void publishProperty(Property property) {
        property.setImageOrder(ImageOrder.normalize(property.getImageOrder(), property.getImages().size()));
        propertyRepository.save(property);
    }
    @Transactional(readOnly = true)
    public List<Property> getPropertyList() { return propertyRepository.findAll(); }

    public void findPropertyByName(String name) throws RepositoryException {
        assertUniqueName(name, null);
    }
    public void assertUniqueName(String name, UUID exceptId) throws RepositoryException {
        Optional<Property> existing = propertyRepository.findByName(name);
        if (existing.isPresent() && !Objects.equals(existing.get().getId(), exceptId))
            throw new RepositoryException("Ya existe una propiedad con ese nombre");
    }

    @Transactional
    public void deleteProperty(UUID propertyId) {
        Property property = getByIdToEdit(propertyId);
        // Only remove files referenced by this property, never a client-supplied directory name.
        List<String> paths = property.getImages().stream()
                .flatMap(image -> java.util.stream.Stream.of(image.getUrl(), image.getThumbnailUrl())).toList();
        fileStorageService.deleteAfterCommit(paths);
        propertyRepository.delete(property);
    }

    @Transactional(readOnly = true)
    public List<PropertyResponseDto> getFeaturedProperties() {
        return propertyRepository.findFeaturedPropertiesWithImages().stream().map(p -> toResponse(p, true)).toList();
    }
    @Transactional(readOnly = true)
    public List<PropertyResponseDto> getLastProperties() {
        return propertyRepository.findTop10ByOrderByCreatedAtDesc().stream().map(p -> toResponse(p, true)).toList();
    }
    @Transactional(readOnly = true)
    public List<PropertyResponseDto> getAllProperties() {
        return propertyRepository.findAll().stream().map(p -> toResponse(p, true)).toList();
    }
    @Transactional(readOnly = true)
    public Page<PropertyResponseDto> getPaginatedProperties(int offset, int limit, String type, String category, String location) {
        if (offset < 0 || limit < 1 || limit > 100)
            throw new IllegalArgumentException("Página inválida: offset debe ser >= 0 y limit debe estar entre 1 y 100");
        Pageable pageable = PageRequest.of(offset, limit, Sort.by(Sort.Direction.DESC, "createdAt").and(Sort.by("id")));
        return propertyRepository.findAllWithFilters(filter(type), filter(category), filter(location), pageable)
                .map(p -> toResponse(p, true));
    }
    private String filter(String value) { return value == null || value.isBlank() ? null : value.trim(); }

    @Transactional(readOnly = true)
    public PropertyResponseDto getById(UUID id) { return toResponse(getByIdToEdit(id), false); }
    public Property getByIdToEdit(UUID id) {
        return propertyRepository.findById(id).orElseThrow(() -> new NotFoundException("Propiedad no encontrada"));
    }

    private PropertyResponseDto toResponse(Property property, boolean summary) {
        // Convert detached copies: modifying managed images in a GET can corrupt stored paths.
        PropertyResponseDto dto = PropertyMapper.INSTANCE.toDto(property);
        List<PropertyImages> source = property.getImages() == null ? List.of() : property.getImages();
        List<Integer> order = ImageOrder.normalize(property.getImageOrder(), source.size());
        List<PropertyImages> images = new ArrayList<>();
        if (summary) {
            if (!order.isEmpty()) images.add(publicImage(source.get(order.getFirst())));
            dto.setImageOrder(images.isEmpty() ? List.of() : List.of(0));
        } else {
            source.forEach(image -> images.add(publicImage(image)));
            dto.setImageOrder(order);
        }
        dto.setImages(images);
        return dto;
    }
    private PropertyImages publicImage(PropertyImages image) {
        PropertyImages copy = new PropertyImages();
        copy.setId(image.getId());
        copy.setUrl(urlGenerator.generateUrlList(image.getUrl()));
        copy.setThumbnailUrl(urlGenerator.generateUrlList(image.getThumbnailUrl()));
        return copy;
    }
}
