package com.mbarca.ByR.service;
import com.mbarca.ByR.model.AdminUser;
import com.mbarca.ByR.repository.AdminUserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.nio.charset.StandardCharsets;
@Service
public class AdminAccountService {
    private final AdminUserRepository users;
    private final PasswordEncoder encoder;
    public AdminAccountService(AdminUserRepository users, PasswordEncoder encoder) {
        this.users = users; this.encoder = encoder;
    }
    public static void validatePassword(String password) {
        if (password == null || password.length() < 12 || password.getBytes(StandardCharsets.UTF_8).length > 72)
            throw new IllegalArgumentException("La contraseña debe tener al menos 12 caracteres y un máximo de 72 bytes");
    }
    @Transactional public void bootstrap(String username, String password) {
        // Initial credentials never overwrite an existing account after a restart.
        if (users.count() != 0 || username.isBlank() || password.isBlank()) return;
        validatePassword(password);
        if (username.trim().length() > 100) throw new IllegalArgumentException("Usuario demasiado largo");
        AdminUser user = new AdminUser();
        user.setUsername(username.trim()); user.setPasswordHash(encoder.encode(password)); users.save(user);
    }
    @Transactional public void changePassword(String username, String currentPassword, String newPassword) {
        validatePassword(newPassword);
        var user = users.findByUsername(username).orElseThrow(() -> new IllegalArgumentException("Usuario inexistente"));
        if (currentPassword == null || currentPassword.getBytes(StandardCharsets.UTF_8).length > 72
                || !encoder.matches(currentPassword, user.getPasswordHash()))
            throw new IllegalArgumentException("La contraseña actual es incorrecta");
        if (encoder.matches(newPassword, user.getPasswordHash()))
            throw new IllegalArgumentException("La nueva contraseña debe ser diferente");
        user.setPasswordHash(encoder.encode(newPassword));
        user.setFailedAttempts(0); user.setLockedUntil(null); users.save(user);
    }
}
