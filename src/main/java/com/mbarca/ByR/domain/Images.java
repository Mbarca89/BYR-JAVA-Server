package com.mbarca.ByR.domain;

import lombok.*;

import java.util.List;

@Setter
@Getter
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
