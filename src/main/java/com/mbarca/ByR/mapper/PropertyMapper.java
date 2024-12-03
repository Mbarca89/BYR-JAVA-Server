package com.mbarca.ByR.mapper;

import com.mbarca.ByR.dto.Response.PropertyListResponseDto;
import com.mbarca.ByR.dto.Response.PropertyPaginatedResponseDto;
import com.mbarca.ByR.dto.Response.PropertyResponseDto;
import com.mbarca.ByR.model.Property;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface PropertyMapper {
    PropertyMapper INSTANCE = Mappers.getMapper(PropertyMapper.class);

    PropertyListResponseDto toListDto (Property property);
    @Mapping(source = "imageOrder", target = "imageOrder")
    PropertyResponseDto toDto (Property property);
}
