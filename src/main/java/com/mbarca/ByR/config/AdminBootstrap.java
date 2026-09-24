package com.mbarca.ByR.config;
import com.mbarca.ByR.service.AdminAccountService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.*;
import org.springframework.stereotype.Component;
@Component
public class AdminBootstrap implements ApplicationRunner {
    private final AdminAccountService accounts;
    @Value("${app.admin.initial-username:}") private String username;
    @Value("${app.admin.initial-password:}") private String password;
    public AdminBootstrap(AdminAccountService accounts) { this.accounts = accounts; }
    @Override public void run(ApplicationArguments args) { accounts.bootstrap(username, password); }
}
