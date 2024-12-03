package com.mbarca.ByR.repository;

import com.mbarca.ByR.model.Property;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PropertyRepository extends JpaRepository<Property, UUID> {

    @Query("SELECT p FROM Property p LEFT JOIN FETCH p.images i WHERE p.featured = true")
    List<Property> findFeaturedPropertiesWithImages();
    @Query("SELECT p FROM Property p ORDER BY p.createdAt DESC")
    List<Property> findTop10ByOrderByCreatedAtDesc();
    Optional<Property> findByName(String name);
    @Query("SELECT COUNT(p) FROM Property p")
    long countAll();
    @Query("SELECT p FROM Property p")
    Page<Property> findAllWithPagination(Pageable pageable);

    @Query("SELECT p FROM Property p " +
            "WHERE (:type IS NULL OR p.type = :type) " +
            "AND (:category IS NULL OR p.category = :category) " +
            "AND (:location IS NULL OR p.location = :location)")
    Page<Property> findAllWithFilters(@Param("type") String type,
                                      @Param("category") String category,
                                      @Param("location") String location,
                                      Pageable pageable);
}
