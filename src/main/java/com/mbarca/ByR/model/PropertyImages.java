package com.mbarca.ByR.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "property_images")
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

        public UUID getId() {
                return id;
        }

        public void setId(UUID id) {
                this.id = id;
        }

        public Property getProperty() {
                return property;
        }

        public void setProperty(Property property) {
                this.property = property;
        }

        public String getUrl() {
                return url;
        }

        public void setUrl(String url) {
                this.url = url;
        }

        public String getThumbnailUrl() {
                return thumbnailUrl;
        }

        public void setThumbnailUrl(String thumbnailUrl) {
                this.thumbnailUrl = thumbnailUrl;
        }
}
