package primeholding.rushhour.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import primeholding.rushhour.entities.Role;
import primeholding.rushhour.entities.RoleName;
import primeholding.rushhour.entities.User;
import primeholding.rushhour.services.RoleService;
import primeholding.rushhour.services.UserService;

import java.util.Collections;
import java.util.NoSuchElementException;
import java.util.Optional;

@Component
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

            Optional<Role> optionalRole = this.roleService.findByName(RoleName.ADMIN);
            if (!optionalRole.isPresent()) {
                throw new NoSuchElementException();
            }
            admin.setRoles(Collections.singleton(optionalRole.get()));

            this.userService.register(admin);
        }
    }
}
