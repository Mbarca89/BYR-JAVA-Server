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
public class PropertyListResponseDto {
    private UUID id;
    private String name;
    private String type;
    private String category;
    private String location;
    private Boolean featured;
}
