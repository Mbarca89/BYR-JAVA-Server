package com.mbarca.ByR.service;
import com.mbarca.ByR.exceptions.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;

@Service
public class FileStorageService {
    private static final Logger log = LoggerFactory.getLogger(FileStorageService.class);
    private final Path rootLocation;
    public FileStorageService(@Value("${file.storage.location}") String location) {
        try {
            Path root = Paths.get(location).toAbsolutePath().normalize();
            Files.createDirectories(root);
            rootLocation = root.toRealPath();
        } catch (IOException e) { throw new IllegalStateException("No se pudo inicializar el almacenamiento", e); }
    }

    public Path checkedPath(String path) {
        Path candidate = Paths.get(path);
        if (!candidate.isAbsolute()) candidate = rootLocation.resolve(candidate);
        candidate = candidate.toAbsolutePath().normalize();
        if (candidate.equals(rootLocation) || !candidate.startsWith(rootLocation))
            throw new IllegalArgumentException("Ruta de archivo inválida");
        // Check existing ancestors too; normalization alone does not prevent symbolic-link escapes.
        Path ancestor = candidate;
        try {
            while (ancestor != null && !Files.exists(ancestor, LinkOption.NOFOLLOW_LINKS)) ancestor = ancestor.getParent();
            if (ancestor == null || !ancestor.toRealPath().startsWith(rootLocation))
                throw new IllegalArgumentException("Ruta de archivo inválida");
        } catch (IOException e) { throw new IllegalArgumentException("Ruta de archivo inválida", e); }
        return candidate;
    }

    public List<String> store(MultipartFile[] files, String subDir) {
        if (!subDir.matches("[a-zA-Z0-9-]+")) throw new IllegalArgumentException("Directorio de imágenes inválido");
        Path directory = checkedPath(subDir);
        List<String> written = new ArrayList<>();
        try {
            Files.createDirectories(directory);
            for (MultipartFile file : files) {
                if (file.isEmpty()) throw new IllegalArgumentException("No se recibió ningún archivo");
                Path destination = checkedPath(directory.resolve(UUID.randomUUID() + ".jpg").toString());
                try (var stream = file.getInputStream()) { Files.copy(stream, destination); }
                written.add(destination.toString());
            }
            if (TransactionSynchronizationManager.isSynchronizationActive()) {
                List<String> rollbackPaths = List.copyOf(written);
                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                    @Override public void afterCompletion(int status) {
                        if (status != STATUS_COMMITTED) cleanup(rollbackPaths);
                    }
                });
            }
            return written;
        } catch (IOException | RuntimeException e) {
            cleanup(written);
            throw new IllegalStateException("No se pudieron guardar las imágenes", e);
        }
    }

    public Resource loadImage(String property, String filename) {
        if (property.contains("/") || property.contains("\\") || filename.contains("/") || filename.contains("\\"))
            throw new IllegalArgumentException("Ruta de archivo inválida");
        Path path = checkedPath(rootLocation.resolve(property).resolve(filename).toString());
        if (!Files.isRegularFile(path) || !Files.isReadable(path)) throw new NotFoundException("Imagen no encontrada");
        return new FileSystemResource(path);
    }

    public void deleteAfterCommit(List<String> paths) {
        // Fail before deleting DB records when a stored path is outside the configured root.
        paths.forEach(this::checkedPath);
        if (!TransactionSynchronizationManager.isSynchronizationActive())
            throw new IllegalStateException("La eliminación de imágenes requiere una transacción");
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override public void afterCommit() { cleanup(paths); }
        });
    }

    private void cleanup(List<String> paths) {
        for (String stored : paths) {
            try {
                Path path = checkedPath(stored);
                Files.deleteIfExists(path);
                Path directory = path.getParent();
                if (!directory.equals(rootLocation)) {
                    try (var remaining = Files.list(directory)) {
                        if (remaining.findAny().isEmpty()) Files.deleteIfExists(directory);
                    } catch (NoSuchFileException ignored) { }
                }
            } catch (IOException | RuntimeException e) {
                // A failed cleanup must not undo a committed database operation.
                log.error("No se pudo limpiar una imagen; revisar el almacenamiento", e);
            }
        }
    }
}
