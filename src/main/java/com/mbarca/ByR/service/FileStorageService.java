package com.mbarca.ByR.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileStorageService {
    private final Path rootLocation;

    public FileStorageService(@Value("${file.storage.location}") String storageLocation) {
        this.rootLocation = Paths.get(storageLocation);
        init();
    }

    public void init() {
        try {
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            throw new RuntimeException("No se pudo inicializar el almacenamiento", e);
        }
    }

    public List<String> store(MultipartFile[] files, String subDir) {
        Path subDirPath = rootLocation.resolve(subDir);
        try {
            Files.createDirectories(subDirPath);
            return Stream.of(files).map(file -> {
                try {
                    if (file.isEmpty()) {
                        throw new RuntimeException("No se recibió ningún archivo");
                    }

                    String originalFilename = Objects.requireNonNull(file.getOriginalFilename());
                    String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                    String newFilename = timestamp + "_" + originalFilename;

                    Path destinationFile = subDirPath.resolve(
                                    Paths.get(newFilename))
                            .normalize().toAbsolutePath();
                    if (Files.exists(destinationFile)) {
                        throw new RuntimeException("Ya existe un archivo con el nombre " + newFilename);
                    }
                    Files.copy(file.getInputStream(), destinationFile,
                            StandardCopyOption.REPLACE_EXISTING);
                    return destinationFile.toString();
                } catch (IOException e) {
                    throw new RuntimeException("Error al guardar el archivo", e);
                }
            }).collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException("Error al crear el directorio.", e);
        }
    }

    public Resource loadAsResource(String filename) {
        try {
            Path file = load(filename);
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException("Error al leer el archivo: " + filename);
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException("Error al leer el archivo: " + filename, e);
        }
    }

    private Path load(String filename) {
        return rootLocation.resolve(filename);
    }
}
