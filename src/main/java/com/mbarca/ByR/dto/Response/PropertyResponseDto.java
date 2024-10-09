package com.mbarca.ByR.dto.Response;

import com.mbarca.ByR.model.PropertyImages;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PropertyResponseDto {
    private UUID id;
    private String name;
    private String description;
    private String type;
    private String category;
    private Integer price;
    private String currency;
    private String location;
    private Integer size;
    private Integer constructed;
    private Integer bedrooms;
    private Integer bathrooms;
    private Integer kitchen;
    private Integer garage;
    private List<String> others;
    private List<String> services;
    private List<String> amenities;
    private Boolean featured;
    private List<PropertyImages> images;
    private List<Integer> imageOrder;
    private java.util.Date createdAt;
    private java.util.Date updatedAt;
}
