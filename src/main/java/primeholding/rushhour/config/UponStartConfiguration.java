package primeholding.rushhour.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import primeholding.rushhour.entities.Role;
import primeholding.rushhour.entities.RoleName;
import primeholding.rushhour.entities.User;
import primeholding.rushhour.services.RoleService;
import primeholding.rushhour.services.UserService;

import java.util.Collections;
import java.util.NoSuchElementException;

@Configuration
public class UponStartConfiguration {
    private static final String ADMIN_EMAIL = "yoda@jade.com";

    private UserService userService;

    private RoleService roleService;

    private PasswordEncoder passwordEncoder;

    @Autowired
    public UponStartConfiguration(UserService userService, RoleService roleService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.roleService = roleService;
        this.passwordEncoder = passwordEncoder;
    }

    @EventListener
    public void addRolesIfNotExist(ApplicationReadyEvent event) {
        if (!this.userService.existWithEmail(ADMIN_EMAIL)) {
            User admin = new User();
            admin.setFirstName("Minch");
            admin.setLastName("Yoda");
            admin.setEmail(ADMIN_EMAIL);
            admin.setPassword(this.passwordEncoder.encode("1234"));

            Role role = this.roleService.findByName(RoleName.ADMIN).orElseThrow(NoSuchElementException::new);

            admin.setRoles(Collections.singleton(role));

            this.userService.register(admin);
        }
    }
}
