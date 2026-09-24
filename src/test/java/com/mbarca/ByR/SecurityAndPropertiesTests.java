package com.mbarca.ByR;

import com.fasterxml.jackson.databind.*;
import com.mbarca.ByR.model.*;
import com.mbarca.ByR.repository.*;
import com.mbarca.ByR.service.AdminAccountService;
import com.mbarca.ByR.service.FileStorageService;
import com.mbarca.ByR.utils.ImageOrder;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.*;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import java.util.*;
import java.nio.file.*;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {"debug=false", "logging.level.root=WARN"})
@AutoConfigureMockMvc
class SecurityAndPropertiesTests {
    private static final String PASSWORD = "Test-password-only-2026";
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired AdminAccountService accounts;
    @Autowired AdminUserRepository users;
    @Autowired PropertyRepository properties;
    @Autowired PasswordEncoder encoder;
    @Autowired FileStorageService storage;
    @Autowired PlatformTransactionManager transactionManager;

    @BeforeEach void prepare() {
        properties.deleteAll(); users.deleteAll();
        accounts.bootstrap("admin", PASSWORD);
    }

    private MockHttpSession csrfSession() throws Exception {
        return (MockHttpSession) mvc.perform(get("/api/auth/csrf")).andExpect(status().isOk())
                .andReturn().getRequest().getSession();
    }
    private String token(MockHttpSession session) throws Exception {
        return json.readTree(mvc.perform(get("/api/auth/csrf").session(session)).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString()).get("token").asText();
    }
    private ResultActions attempt(MockHttpSession session, String password) throws Exception {
        return mvc.perform(post("/api/auth/login").session(session).header("X-CSRF-TOKEN", token(session))
                .param("username", "admin").param("password", password));
    }
    private MockHttpSession login() throws Exception {
        MockHttpSession session = csrfSession();
        return (MockHttpSession) attempt(session, PASSWORD).andExpect(status().isOk()).andReturn().getRequest().getSession();
    }

    @Test void publicReadsAndProtectedMutations() throws Exception {
        mvc.perform(get("/api/properties")).andExpect(status().isOk());
        mvc.perform(get("/api/properties/getPropertyList")).andExpect(status().isUnauthorized());
        MockHttpSession anonymous = csrfSession();
        String token = token(anonymous);
        mvc.perform(post("/api/properties/publish").session(anonymous).header("X-CSRF-TOKEN", token))
                .andExpect(status().isUnauthorized());
        mvc.perform(put("/api/properties/edit/" + UUID.randomUUID()).session(anonymous).header("X-CSRF-TOKEN", token))
                .andExpect(status().isUnauthorized());
        mvc.perform(delete("/api/images/delete").session(anonymous).header("X-CSRF-TOKEN", token)
                .param("id", UUID.randomUUID().toString())).andExpect(status().isUnauthorized());
        mvc.perform(delete("/api/properties/deleteProperty").session(anonymous).header("X-CSRF-TOKEN", token)
                .param("propertyId", UUID.randomUUID().toString())).andExpect(status().isUnauthorized());
    }

    @Test void csrfRequiredForLoginAndMutationAndSessionRotates() throws Exception {
        MockHttpSession session = csrfSession();
        String originalId = session.getId();
        mvc.perform(post("/api/auth/login").session(session).param("username", "admin").param("password", PASSWORD))
                .andExpect(status().isForbidden());
        attempt(session, PASSWORD).andExpect(status().isOk());
        assertThat(session.getId()).isNotEqualTo(originalId);
        mvc.perform(get("/api/auth/me").session(session)).andExpect(status().isOk()).andExpect(jsonPath("$.username").value("admin"));
        mvc.perform(delete("/api/properties/deleteProperty").session(session).param("propertyId", UUID.randomUUID().toString()))
                .andExpect(status().isForbidden());
    }

    @Test void passwordsAreHashedAndBootstrapNeverResetsAnAccount() {
        accounts.bootstrap("replacement", "Another-password-2026");
        var user = users.findAll().getFirst();
        assertThat(users.count()).isEqualTo(1);
        assertThat(user.getPasswordHash()).isNotEqualTo(PASSWORD);
        assertThat(encoder.matches(PASSWORD, user.getPasswordHash())).isTrue();
        assertThat(user.getUsername()).isEqualTo("admin");
    }

    @Test void repeatedBadPasswordsLockAccount() throws Exception {
        MockHttpSession session = csrfSession();
        for (int i = 0; i < 5; i++) attempt(session, "incorrect").andExpect(status().isUnauthorized());
        attempt(session, PASSWORD).andExpect(status().isUnauthorized());
        assertThat(users.findAll().getFirst().getLockedUntil()).isNotNull();
    }

    @Test void passwordChangeChecksCurrentPasswordAndRevokesAllSessions() throws Exception {
        MockHttpSession first = login();
        MockHttpSession second = login();
        mvc.perform(post("/api/auth/password").session(first).header("X-CSRF-TOKEN", token(first))
                .contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(Map.of(
                        "currentPassword", "incorrect", "newPassword", "New-password-2026"))))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/api/auth/password").session(first).header("X-CSRF-TOKEN", token(first))
                .contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(Map.of(
                        "currentPassword", PASSWORD, "newPassword", "short"))))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/api/auth/password").session(first).header("X-CSRF-TOKEN", token(first))
                .contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(Map.of(
                        "currentPassword", PASSWORD, "newPassword", "New-password-2026"))))
                .andExpect(status().isOk());
        assertThat(first.isInvalid()).isTrue();
        mvc.perform(get("/api/auth/me").session(second)).andExpect(status().isUnauthorized());
        MockHttpSession newSession = csrfSession();
        attempt(newSession, PASSWORD).andExpect(status().isUnauthorized());
        attempt(newSession, "New-password-2026").andExpect(status().isOk());
    }

    @Test void logoutInvalidatesSessionAndUnknownOriginsAreRejected() throws Exception {
        MockHttpSession session = login();
        mvc.perform(post("/api/auth/logout").session(session).header("X-CSRF-TOKEN", token(session))).andExpect(status().isOk());
        assertThat(session.isInvalid()).isTrue();
        mvc.perform(get("/api/auth/me")).andExpect(status().isUnauthorized());
        mvc.perform(options("/api/auth/login").header("Origin", "https://untrusted.invalid")
                .header("Access-Control-Request-Method", "POST")).andExpect(status().isForbidden());
        mvc.perform(options("/api/auth/login").header("Origin", "http://localhost:3000")
                .header("Access-Control-Request-Method", "POST").header("Access-Control-Request-Headers", "X-CSRF-TOKEN"))
                .andExpect(status().isOk()).andExpect(header().string("Access-Control-Allow-Credentials", "true"));
    }

    private Property property(String name) {
        Property p = new Property();
        p.setName(name); p.setType("Casa"); p.setCategory("Venta"); p.setPrice(0); p.setCurrency("$"); p.setLocation("San Luis");
        p.setSize(0); p.setConstructed(0); p.setBedrooms(0); p.setBathrooms(0); p.setKitchen(0); p.setGarage(0);
        p.setOthers(new ArrayList<>()); p.setServices(new ArrayList<>()); p.setAmenities(new ArrayList<>());
        p.setFeatured(true); p.setImages(new ArrayList<>()); p.setImageOrder(new ArrayList<>());
        return p;
    }

    @Test void emptyPhotosAndBrokenOrdersNeverCrashReads() throws Exception {
        Property p = property("Sin fotos"); p.setImageOrder(Arrays.asList(9, -1, null));
        p = properties.saveAndFlush(p);
        for (String path : List.of("/api/properties", "/api/properties/last", "/api/properties/featured")) {
            mvc.perform(get(path)).andExpect(status().isOk()).andExpect(jsonPath("$[0].images").isEmpty());
        }
        mvc.perform(get("/api/properties/getById").param("propertyId", p.getId().toString()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.imageOrder").isEmpty());
        assertThat(ImageOrder.normalize(Arrays.asList(2, 2, -1, 80, null), 3)).containsExactly(2, 0, 1);
    }

    @Test void lastReallyLimitsToTenAndPaginationValidatesBounds() throws Exception {
        for (int i = 0; i < 12; i++) properties.save(property("Propiedad " + i));
        mvc.perform(get("/api/properties/last")).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(10));
        mvc.perform(get("/api/properties/paginated").param("limit", "6").param("offset", "0")
                .param("location", "San Luis")).andExpect(status().isOk()).andExpect(jsonPath("$.page.totalElements").value(12));
        mvc.perform(get("/api/properties/paginated").param("limit", "0").param("offset", "-1")).andExpect(status().isBadRequest());
        mvc.perform(get("/api/properties/getById").param("propertyId", UUID.randomUUID().toString())).andExpect(status().isNotFound());
        mvc.perform(get("/api/properties/getById").param("propertyId", "bad-id")).andExpect(status().isBadRequest());
    }

    @Test void photoDeletionPreservesOrderAndReadsDontChangePaths() throws Exception {
        Property p = property("Fotos");
        List<String> stored = storage.store(new MockMultipartFile[] {
                new MockMultipartFile("image", "a.jpg", "image/jpeg", new byte[]{1}),
                new MockMultipartFile("image", "b.jpg", "image/jpeg", new byte[]{2}),
                new MockMultipartFile("image", "c.jpg", "image/jpeg", new byte[]{3}) }, UUID.randomUUID().toString());
        for (String path : stored) {
            PropertyImages image = new PropertyImages(); image.setUrl(path); image.setThumbnailUrl(path); image.setProperty(p);
            p.getImages().add(image);
        }
        p.setImageOrder(new ArrayList<>(List.of(2, 0, 1))); p = properties.saveAndFlush(p);
        UUID id = p.getId();
        UUID removeId = p.getImages().get(0).getId();
        mvc.perform(get("/api/properties/getById").param("propertyId", id.toString())).andExpect(status().isOk())
                .andExpect(jsonPath("$.images[0].url").value(org.hamcrest.Matchers.startsWith("http://localhost:8080/api/images/")));
        new TransactionTemplate(transactionManager).executeWithoutResult(tx -> {
            assertThat(properties.findById(id).orElseThrow().getImages().getFirst().getUrl()).isEqualTo(stored.getFirst());
        });
        MockHttpSession session = login();
        mvc.perform(delete("/api/images/delete").session(session).header("X-CSRF-TOKEN", token(session))
                .param("id", removeId.toString())).andExpect(status().isOk());
        mvc.perform(get("/api/properties/getById").param("propertyId", id.toString())).andExpect(status().isOk())
                .andExpect(jsonPath("$.images.length()").value(2))
                .andExpect(jsonPath("$.imageOrder[0]").value(1)).andExpect(jsonPath("$.imageOrder[1]").value(0));
        assertThat(Files.exists(Path.of(stored.getFirst()))).isFalse();
        // Renaming never requires deleting a directory by its new display name.
        mvc.perform(delete("/api/properties/deleteProperty").session(session).header("X-CSRF-TOKEN", token(session))
                .param("propertyId", id.toString()).param("propertyName", "../../ignored"))
                .andExpect(status().isOk());
        assertThat(properties.findById(id)).isEmpty();
        assertThat(Files.exists(Path.of(stored.get(1)))).isFalse();
    }

    @Test void multipartCreationAndEditingValidateAndAllowNoImages() throws Exception {
        MockHttpSession session = login();
        Property p = property("Nueva"); p.setId(UUID.randomUUID());
        mvc.perform(multipart("/api/properties/publish").session(session).header("X-CSRF-TOKEN", token(session))
                .param("propertyData", json.writeValueAsString(p))).andExpect(status().isOk());
        Property saved = properties.findByName("Nueva").orElseThrow();
        assertThat(saved.getId()).isNotEqualTo(p.getId());
        p.setName("Renombrada");
        mvc.perform(multipart("/api/properties/edit/" + saved.getId()).with(request -> { request.setMethod("PUT"); return request; })
                .session(session).header("X-CSRF-TOKEN", token(session)).param("propertyData", json.writeValueAsString(p)))
                .andExpect(status().isOk());
        p.setName(" "); p.setPrice(-1);
        mvc.perform(multipart("/api/properties/publish").session(session).header("X-CSRF-TOKEN", token(session))
                .param("propertyData", json.writeValueAsString(p))).andExpect(status().isBadRequest());
        p.setName("Invalid image"); p.setPrice(0);
        mvc.perform(multipart("/api/properties/publish")
                .file(new MockMultipartFile("images", "fake.png", "image/png", "not an image".getBytes()))
                .session(session).header("X-CSRF-TOKEN", token(session)).param("propertyData", json.writeValueAsString(p)))
                .andExpect(status().isBadRequest());
        assertThat(properties.count()).isEqualTo(1);
    }

    @Test void storageRejectsEscapingPathsAndMissingImagesAre404() throws Exception {
        assertThatThrownBy(() -> storage.checkedPath("../outside.txt")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> storage.loadImage("..", "..")).isInstanceOf(IllegalArgumentException.class);
        mvc.perform(get("/api/images/missing/missing.jpg")).andExpect(status().isNotFound());
    }

    @Test void failedUploadRollsBackBothThePropertyAndPreviouslyWrittenImages() throws Exception {
        MockHttpSession session = login();
        var bytes = new java.io.ByteArrayOutputStream();
        javax.imageio.ImageIO.write(new java.awt.image.BufferedImage(2, 2, java.awt.image.BufferedImage.TYPE_INT_RGB), "png", bytes);
        long before;
        try (var files = Files.walk(Path.of("target/test-images"))) { before = files.filter(Files::isRegularFile).count(); }
        mvc.perform(multipart("/api/properties/publish")
                .file(new MockMultipartFile("images", "valid.png", "image/png", bytes.toByteArray()))
                .file(new MockMultipartFile("images", "invalid.png", "image/png", "bad".getBytes()))
                .session(session).header("X-CSRF-TOKEN", token(session))
                .param("propertyData", json.writeValueAsString(property("Rollback"))))
                .andExpect(status().isBadRequest());
        assertThat(properties.count()).isZero();
        try (var files = Files.walk(Path.of("target/test-images"))) {
            assertThat(files.filter(Files::isRegularFile).count()).isEqualTo(before);
        }
    }
}
