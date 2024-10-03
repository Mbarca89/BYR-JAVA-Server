package com.mbarca.ByR.repository;

import com.mbarca.ByR.model.Property;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PropertyRepository extends JpaRepository<Property, UUID> {
}
