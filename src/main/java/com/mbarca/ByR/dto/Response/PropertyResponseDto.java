package com.mbarca.ByR.dto.Response;

import com.mbarca.ByR.model.PropertyImages;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;
import java.util.UUID;


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

    public PropertyResponseDto() {
    }

    public PropertyResponseDto(UUID id, String name, String description, String type, String category, Integer price, String currency, String location, Integer size, Integer constructed, Integer bedrooms, Integer bathrooms, Integer kitchen, Integer garage, List<String> others, List<String> services, List<String> amenities, Boolean featured, List<PropertyImages> images, List<Integer> imageOrder, Date createdAt, Date updatedAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.type = type;
        this.category = category;
        this.price = price;
        this.currency = currency;
        this.location = location;
        this.size = size;
        this.constructed = constructed;
        this.bedrooms = bedrooms;
        this.bathrooms = bathrooms;
        this.kitchen = kitchen;
        this.garage = garage;
        this.others = others;
        this.services = services;
        this.amenities = amenities;
        this.featured = featured;
        this.images = images;
        this.imageOrder = imageOrder;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public Integer getPrice() {
        return price;
    }

    public void setPrice(Integer price) {
        this.price = price;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public Integer getConstructed() {
        return constructed;
    }

    public void setConstructed(Integer constructed) {
        this.constructed = constructed;
    }

    public Integer getBedrooms() {
        return bedrooms;
    }

    public void setBedrooms(Integer bedrooms) {
        this.bedrooms = bedrooms;
    }

    public Integer getBathrooms() {
        return bathrooms;
    }

    public void setBathrooms(Integer bathrooms) {
        this.bathrooms = bathrooms;
    }

    public Integer getKitchen() {
        return kitchen;
    }

    public void setKitchen(Integer kitchen) {
        this.kitchen = kitchen;
    }

    public Integer getGarage() {
        return garage;
    }

    public void setGarage(Integer garage) {
        this.garage = garage;
    }

    public List<String> getOthers() {
        return others;
    }

    public void setOthers(List<String> others) {
        this.others = others;
    }

    public List<String> getServices() {
        return services;
    }

    public void setServices(List<String> services) {
        this.services = services;
    }

    public List<String> getAmenities() {
        return amenities;
    }

    public void setAmenities(List<String> amenities) {
        this.amenities = amenities;
    }

    public Boolean getFeatured() {
        return featured;
    }

    public void setFeatured(Boolean featured) {
        this.featured = featured;
    }

    public List<PropertyImages> getImages() {
        return images;
    }

    public void setImages(List<PropertyImages> images) {
        this.images = images;
    }

    public List<Integer> getImageOrder() {
        return imageOrder;
    }

    public void setImageOrder(List<Integer> imageOrder) {
        this.imageOrder = imageOrder;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }
}
