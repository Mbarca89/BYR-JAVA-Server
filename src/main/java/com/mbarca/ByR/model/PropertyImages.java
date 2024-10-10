package com.mbarca.ByR.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "property_images")
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class PropertyImages {
        @Id
        @GeneratedValue
        private UUID id;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "property_id", nullable = false)
        @JsonIgnore
        private Property property;

        @Column(nullable = false)
        private String url;

        @Column(nullable = false)
        private String thumbnailUrl;
}
