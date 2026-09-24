package com.mbarca.ByR.service;
import com.mbarca.ByR.exceptions.NotFoundException;
import com.mbarca.ByR.repository.ImageRepository;
import com.mbarca.ByR.utils.ImageOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;
@Service
public class ImageService {
    @Autowired ImageRepository imageRepository;
    @Autowired FileStorageService fileStorageService;
    @Transactional public String deleteImage(UUID id) {
        var image = imageRepository.findById(id).orElseThrow(() -> new NotFoundException("Imagen no encontrada"));
        var property = image.getProperty();
        var images = property.getImages();
        int removed = images.indexOf(image);
        property.setImageOrder(ImageOrder.afterRemoval(property.getImageOrder(), images.size(), removed));
        images.remove(removed);
        fileStorageService.deleteAfterCommit(List.of(image.getUrl(), image.getThumbnailUrl()));
        return "Imagen eliminada correctamente";
    }
}
