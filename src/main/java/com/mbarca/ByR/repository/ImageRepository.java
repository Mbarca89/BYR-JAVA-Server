package com.mbarca.ByR.repository;

import com.mbarca.ByR.model.PropertyImages;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ImageRepository extends JpaRepository<PropertyImages, UUID> {

}
