package com.mbarca.ByR.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Images {
    byte [] fullImage;
    byte [] thumbnail;
    List<String> paths;

    public Images(byte[] fullImage, byte[] thumbnail) {
        this.fullImage = fullImage;
        this.thumbnail = thumbnail;
    }

}
