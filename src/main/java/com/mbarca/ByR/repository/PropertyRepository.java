package com.mbarca.ByR.repository;

import com.mbarca.ByR.model.Property;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface PropertyRepository extends JpaRepository<Property, UUID> {

    @Query("SELECT p FROM Property p LEFT JOIN FETCH p.images i WHERE p.featured = true ORDER BY i.id ASC")
    List<Property> findFeaturedPropertiesWithImages();
    @Query("SELECT p FROM Property p ORDER BY p.createdAt DESC")
    List<Property> findTop10ByOrderByCreatedAtDesc();
}
