package com.mbarca.ByR.controller;
import com.mbarca.ByR.service.AdminAccountService;
import jakarta.servlet.http.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
@RestController @RequestMapping("/api/auth")
public class AuthController {
    private final AdminAccountService accounts;
    private final SessionRegistry sessions;
    public AuthController(AdminAccountService accounts, SessionRegistry sessions) {
        this.accounts = accounts; this.sessions = sessions;
    }
    @GetMapping("/csrf") public Map<String, String> csrf(CsrfToken token) {
        return Map.of("headerName", token.getHeaderName(), "token", token.getToken());
    }
    @GetMapping("/me") public Map<String, String> me(Authentication auth) {
        return Map.of("username", auth.getName());
    }
    public record PasswordChange(@NotBlank @Size(max = 72) String currentPassword,
                                 @NotBlank @Size(min = 12, max = 72) String newPassword) {}
    @PostMapping("/password")
    public Map<String, String> changePassword(@Valid @RequestBody PasswordChange change, Authentication auth,
                                              HttpServletRequest request, HttpServletResponse response) {
        accounts.changePassword(auth.getName(), change.currentPassword(), change.newPassword());
        sessions.getAllSessions(auth.getPrincipal(), false).forEach(session -> session.expireNow());
        new SecurityContextLogoutHandler().logout(request, response, auth);
        return Map.of("message", "Contraseña actualizada. Iniciá sesión con la nueva contraseña.");
    }
}
