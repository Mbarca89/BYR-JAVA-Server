package com.mbarca.ByR.dto.Response;

import com.mbarca.ByR.model.PropertyImages;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

public class PropertyListResponseDto {
    private UUID id;
    private String name;
    private String type;
    private String category;
    private String location;
    private Boolean featured;

    public PropertyListResponseDto() {
    }

    public PropertyListResponseDto(UUID id, String name, String type, String category, String location, Boolean featured) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.category = category;
        this.location = location;
        this.featured = featured;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Boolean getFeatured() {
        return featured;
    }

    public void setFeatured(Boolean featured) {
        this.featured = featured;
    }
}
