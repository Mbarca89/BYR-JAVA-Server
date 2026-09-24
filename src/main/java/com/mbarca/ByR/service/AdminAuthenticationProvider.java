package com.mbarca.ByR.service;
import com.mbarca.ByR.repository.AdminUserRepository;
import org.springframework.security.authentication.*;
import org.springframework.security.core.*;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
@Component
public class AdminAuthenticationProvider implements AuthenticationProvider {
    private final AdminUserRepository users;
    private final PasswordEncoder encoder;
    private final String dummyHash;
    public AdminAuthenticationProvider(AdminUserRepository users, PasswordEncoder encoder) {
        this.users = users; this.encoder = encoder;
        this.dummyHash = encoder.encode(java.util.UUID.randomUUID().toString());
    }
    @Override @Transactional(noRollbackFor = AuthenticationException.class)
    public Authentication authenticate(Authentication authentication) {
        String password = String.valueOf(authentication.getCredentials());
        var user = users.findByUsername(authentication.getName().trim()).orElse(null);
        boolean validLength = password.getBytes(StandardCharsets.UTF_8).length <= 72;
        boolean matches = encoder.matches(validLength ? password : "", user == null ? dummyHash : user.getPasswordHash());
        if (user == null) throw invalidCredentials();
        Instant now = Instant.now();
        if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(now)) throw invalidCredentials();
        if (user.getLockedUntil() != null) { user.setFailedAttempts(0); user.setLockedUntil(null); }
        if (!validLength || !matches) {
            user.setFailedAttempts(user.getFailedAttempts() + 1);
            if (user.getFailedAttempts() >= 5) user.setLockedUntil(now.plus(15, ChronoUnit.MINUTES));
            users.save(user);
            throw invalidCredentials();
        }
        user.setFailedAttempts(0); user.setLockedUntil(null); users.save(user);
        var principal = User.withUsername(user.getUsername()).password("").roles("ADMIN").build();
        return UsernamePasswordAuthenticationToken.authenticated(principal, null, principal.getAuthorities());
    }
    private BadCredentialsException invalidCredentials() {
        return new BadCredentialsException("Credenciales incorrectas o acceso temporalmente bloqueado");
    }
    @Override public boolean supports(Class<?> type) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(type);
    }
}
