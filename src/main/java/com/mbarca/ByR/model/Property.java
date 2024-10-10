package com.mbarca.ByR.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Setter
@Getter
@Entity
@Table(name = "property")
@AllArgsConstructor
@NoArgsConstructor
public class Property {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private String type;

    @Column(nullable = false)
    private String category;

    @Column(nullable = false)
    private Integer price;

    @Column(nullable = false)
    private String currency;

    @Column(nullable = false)
    private String location;

    @Column(nullable = false)
    private Integer size;

    @Column(nullable = false)
    private Integer constructed;

    @Column(nullable = false, columnDefinition = "integer default 1")
    private Integer bedrooms;

    @Column(nullable = false, columnDefinition = "integer default 1")
    private Integer bathrooms;

    @Column(nullable = false, columnDefinition = "integer default 0")
    private Integer kitchen;

    @Column(nullable = false, columnDefinition = "integer default 0")
    private Integer garage;

    @ElementCollection
    @Column(nullable = false)
    private List<String> others;

    @ElementCollection
    @Column(nullable = false)
    private List<String> services;

    @ElementCollection
    @Column(nullable = false)
    private List<String> amenities;

    @Column(nullable = false, columnDefinition = "boolean default false")
    private Boolean featured;

    @OneToMany(mappedBy = "property", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PropertyImages> images;

    @Column(nullable = true)
    private List<Integer> imageOrder;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private java.util.Date createdAt;

    @Column(name = "updated_at")
    @Temporal(TemporalType.TIMESTAMP)
    private java.util.Date updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = new java.util.Date();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = new java.util.Date();
    }

}
