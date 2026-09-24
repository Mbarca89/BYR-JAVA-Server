package com.mbarca.ByR.model;
import jakarta.persistence.*;
import java.time.Instant;
@Entity @Table(name = "admin_users")
public class AdminUser {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, unique = true, length = 100) private String username;
    @Column(nullable = false) private String passwordHash;
    private int failedAttempts;
    private Instant lockedUntil;
    public Long getId() { return id; }
    public String getUsername() { return username; }
    public void setUsername(String v) { username = v; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String v) { passwordHash = v; }
    public int getFailedAttempts() { return failedAttempts; }
    public void setFailedAttempts(int v) { failedAttempts = v; }
    public Instant getLockedUntil() { return lockedUntil; }
    public void setLockedUntil(Instant v) { lockedUntil = v; }
}
