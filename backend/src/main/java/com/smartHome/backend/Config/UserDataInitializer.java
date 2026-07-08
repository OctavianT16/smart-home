package com.smartHome.backend.Config;

import com.smartHome.backend.authentication.AppRole;
import com.smartHome.backend.authentication.AppUser;
import com.smartHome.backend.authentication.AppUserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class UserDataInitializer implements CommandLineRunner {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;

    public UserDataInitializer(AppUserRepository appUserRepository,
                           PasswordEncoder passwordEncoder) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        createUserIfMissing("admin", "admin123", AppRole.ADMIN);
        createUserIfMissing("user", "user123", AppRole.USER);
    }

    private void createUserIfMissing(String username, String rawPassword, AppRole role) {
        if (!appUserRepository.existsByUsername(username)) {
            AppUser user = new AppUser(
                    username,
                    passwordEncoder.encode(rawPassword),
                    role,
                    true
            );

            appUserRepository.save(user);
            System.out.println("Created default " + role + " user: " + username);
        }
    }
}