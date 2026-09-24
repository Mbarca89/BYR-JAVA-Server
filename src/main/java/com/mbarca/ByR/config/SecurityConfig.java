package com.mbarca.ByR.config;

import com.mbarca.ByR.service.AdminAuthenticationProvider;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.*;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.session.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.session.HttpSessionEventPublisher;

@Configuration
public class SecurityConfig {
    @Bean public PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(12); }
    @Bean public SessionRegistry sessionRegistry() { return new SessionRegistryImpl(); }
    @Bean public HttpSessionEventPublisher httpSessionEventPublisher() { return new HttpSessionEventPublisher(); }

    public static void message(HttpServletResponse response, int status, String message) throws java.io.IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"message\":\"" + message + "\"}");
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, AdminAuthenticationProvider provider,
                                                  SessionRegistry sessions) throws Exception {
        return http.cors(Customizer.withDefaults()).csrf(Customizer.withDefaults())
                .authenticationProvider(provider)
                .requestCache(cache -> cache.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/error").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/auth/csrf").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/properties/getPropertyList").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/properties", "/api/properties/featured",
                                "/api/properties/last", "/api/properties/paginated", "/api/properties/getById",
                                "/api/images/*/*").permitAll()
                        .requestMatchers("/api/**").hasRole("ADMIN")
                        .anyRequest().denyAll())
                .formLogin(form -> form.loginProcessingUrl("/api/auth/login")
                        .successHandler((request, response, auth) -> message(response, 200, "Sesión iniciada"))
                        .failureHandler((request, response, exception) -> message(response, 401,
                                "Credenciales incorrectas o acceso temporalmente bloqueado. Si hubo varios intentos, esperá 15 minutos."))
                        .permitAll())
                .logout(logout -> logout.logoutUrl("/api/auth/logout").deleteCookies("JSESSIONID")
                        .logoutSuccessHandler((request, response, auth) -> message(response, 200, "Sesión cerrada")))
                .exceptionHandling(errors -> errors
                        .authenticationEntryPoint((request, response, exception) -> message(response, 401, "Iniciá sesión para continuar"))
                        .accessDeniedHandler((request, response, exception) -> message(response, 403, "Solicitud no autorizada. Recargá la página e intentá nuevamente.")))
                .sessionManagement(session -> session.maximumSessions(-1).sessionRegistry(sessions)
                        .expiredSessionStrategy(event -> message(event.getResponse(), 401, "La sesión venció. Iniciá sesión nuevamente.")))
                .build();
    }
}
