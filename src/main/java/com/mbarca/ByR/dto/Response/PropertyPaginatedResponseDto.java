package com.mbarca.ByR.dto.Response;

import com.mbarca.ByR.model.Property;

import java.util.List;

public class PropertyPaginatedResponseDto {
    long count;
    List<PropertyResponseDto> properties;

    public PropertyPaginatedResponseDto() {
    }

    public PropertyPaginatedResponseDto(long count, List<PropertyResponseDto> properties) {
        this.count = count;
        this.properties = properties;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public List<PropertyResponseDto> getProperties() {
        return properties;
    }

    public void setProperties(List<PropertyResponseDto> properties) {
        this.properties = properties;
    }
}
