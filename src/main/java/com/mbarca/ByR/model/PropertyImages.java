package com.mbarca.ByR.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "images")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PropertyImages {

        @Id
        @GeneratedValue
        private UUID id;

        @Column(nullable = false)
        private String name;

        @Column(nullable = false)
        private String url;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "property_id", nullable = false)
        private Property property;
}
