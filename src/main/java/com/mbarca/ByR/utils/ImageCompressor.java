package com.mbarca.ByR.utils;

import com.mbarca.ByR.domain.Images;
import com.mbarca.ByR.service.FileStorageService;
import org.imgscalr.Scalr;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifIFD0Directory;

@Service
public class ImageCompressor {
    @Autowired
    private FileStorageService fileStorageService;

    public Images compressImage(byte[] imageData, boolean saveFile, String fileName, String propertyName) throws Exception {
        BufferedImage originalImage = ImageIO.read(new ByteArrayInputStream(imageData));

        // Rotate the image based on EXIF orientation
        BufferedImage rotatedImage = rotateImage(originalImage, imageData);

        // Resize the rotated image to 640x360 or 320x184
        BufferedImage resizedImage = resizeImage(rotatedImage, 1280, 720);
        BufferedImage resizedThumbnail = resizeImage(rotatedImage, 320, 184);

        // Convert resized image and thumbnail to JPG
        byte[] fullImage = convertToJPG(resizedImage);
        byte[] thumbnail = convertToJPG(resizedThumbnail);

        if (saveFile) {
            String subDir = propertyName;
            List<String> paths = saveImages(fullImage, thumbnail, imageData, subDir, fileName);
            return new Images(fullImage, thumbnail, paths);
        } else {
            return new Images(fullImage, thumbnail);
        }
    }

    private  BufferedImage rotateImage(BufferedImage originalImage, byte[] imageData) throws Exception {
        int orientation = 1; // Default orientation (no rotation)
        try {
            Metadata metadata = ImageMetadataReader.readMetadata(new ByteArrayInputStream(imageData));
            Directory directory = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);
            if (directory != null && directory.containsTag(ExifIFD0Directory.TAG_ORIENTATION)) {
                orientation = directory.getInt(ExifIFD0Directory.TAG_ORIENTATION);
            }
        } catch (Exception e) {
            throw new Exception("Error al rotar la imagen: " + e.getMessage());
        }

        switch (orientation) {
            case 3:
                return Scalr.rotate(originalImage, Scalr.Rotation.CW_180);
            case 6:
                return Scalr.rotate(originalImage, Scalr.Rotation.CW_90);
            case 8:
                return Scalr.rotate(originalImage, Scalr.Rotation.CW_270);
            default:
                return originalImage; // No rotation needed
        }
    }

    private  BufferedImage resizeImage(BufferedImage image, int targetWidth, int targetHeight) {
        return Scalr.resize(image, Scalr.Method.QUALITY, Scalr.Mode.AUTOMATIC, targetWidth, targetHeight);
    }

    private  byte[] convertToJPG(BufferedImage image) throws IOException {
        // Convert image to RGB if it has transparency (e.g., PNG with alpha channel)
        if (image.getType() == BufferedImage.TYPE_INT_ARGB || image.getType() == BufferedImage.TYPE_4BYTE_ABGR) {
            BufferedImage newImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
            Graphics2D g = newImage.createGraphics();
            g.setComposite(AlphaComposite.Src);
            g.setColor(Color.WHITE); // Set background color to white
            g.fillRect(0, 0, image.getWidth(), image.getHeight());
            g.drawImage(image, 0, 0, null);
            g.dispose();
            image = newImage;
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpg");
        if (!writers.hasNext()) {
            throw new IllegalStateException("ºNo JPG Image Writers found");
        }
        ImageWriter writer = writers.next();
        ImageWriteParam param = writer.getDefaultWriteParam();
        param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        param.setCompressionQuality(0.85f); // Adjust the quality as needed
        ImageOutputStream ios = ImageIO.createImageOutputStream(outputStream);
        writer.setOutput(ios);
        writer.write(null, new IIOImage(image, null, null), param);
        ios.close();
        writer.dispose();
        return outputStream.toByteArray();
    }

    private  List<String> saveImages(byte[] fullImage, byte[] thumbnail, byte[] originalImage, String subDir, String imageName) throws IOException {
        CustomMultipartFile fullImageFile = new CustomMultipartFile(fullImage, "fullImage", imageName + "_full.jpg", "image/jpeg");
        CustomMultipartFile thumbnailFile = new CustomMultipartFile(thumbnail, "thumbnail", imageName + "_thumb.jpg", "image/jpeg");
        CustomMultipartFile originalFile = new CustomMultipartFile(originalImage, "HD", imageName + "_HD.jpg", "image/jpeg");

        MultipartFile[] files = {fullImageFile, thumbnailFile, originalFile};
        return fileStorageService.store(files, subDir);
    }
}
