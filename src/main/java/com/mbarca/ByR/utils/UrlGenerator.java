package com.mbarca.ByR.utils;

import org.springframework.stereotype.Service;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.file.Path;
import java.nio.file.Paths;


@Service
public class UrlGenerator {

    public String generateUrlList(String filepath) {
        // Convertir la ruta del archivo de Windows a una ruta relativa
        Path path = Paths.get(filepath);

        // Extraer las partes de la ruta
        Path fileName = path.getFileName(); // Nombre del archivo (20241007_093429_mexico a d1.png_full.jpg)
        Path propertyDir = path.getParent(); // Directorio de la propiedad (123)

        // Convertir las rutas a Strings y codificar para URL
        String propertyId = encodeURIComponent(propertyDir.getFileName().toString()); // ID de la propiedad
        String filename = encodeURIComponent(fileName.toString()); // Nombre del archivo

        String baseUrl = "https://inmobiliariabyr.com.ar/api/images/";
        String imageUrl;
        String thumbnailUrl;

        // Si no es una descarga, generar la URL de visualización
        return String.format("%s%s/%s", baseUrl, propertyId, filename);

}

private String encodeURIComponent(String value) {
    try {
        return URLEncoder.encode(value, "UTF-8")
                .replace("+", "%20") // Reemplazar espacios por %20
                .replace("*", "%2A")
                .replace("%7E", "~");
    } catch (UnsupportedEncodingException e) {
        throw new RuntimeException("Error al codificar la URL", e);
    }
}
}
