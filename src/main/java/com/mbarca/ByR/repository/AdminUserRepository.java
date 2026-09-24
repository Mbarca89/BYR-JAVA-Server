package com.mbarca.ByR.repository;
import com.mbarca.ByR.model.AdminUser;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import java.util.Optional;
public interface AdminUserRepository extends JpaRepository<AdminUser, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<AdminUser> findByUsername(String username);
}
