package com.mbarca.ByR.utils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
@Service
public class UrlGenerator {
    @Value("${app.public-base-url:https://inmobiliariabyr.com.ar}") private String baseUrl;
    public String generateUrlList(String filepath) {
        if (filepath == null || filepath.isBlank()) return null;
        String normalized = filepath.replace('\\', '/');
        String[] parts = normalized.split("/");
        if (parts.length < 2) return null;
        return baseUrl.replaceAll("/+$", "") + "/api/images/" + encode(parts[parts.length - 2]) + "/" + encode(parts[parts.length - 1]);
    }
    private String encode(String value) { return URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20"); }
}
