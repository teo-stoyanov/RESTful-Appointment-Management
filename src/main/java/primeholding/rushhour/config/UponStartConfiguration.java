package primeholding.rushhour.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

    private UserService userService;

    private RoleService roleService;

    private PasswordEncoder passwordEncoder;

    @Value("${app.admin.first.name}")
    private String firstName;

    @Value("${app.admin.last.name}")
    private String lastName;

    @Value("${app.admin.email}")
    private String email;

    @Value("${app.admin.password}")
    private String password;

    @Autowired
    public UponStartConfiguration(UserService userService, RoleService roleService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.roleService = roleService;
        this.passwordEncoder = passwordEncoder;
    }

    @EventListener
    public void addRolesIfNotExist(ApplicationReadyEvent event) {
        if (!this.userService.existWithEmail(this.email)) {
            User admin = new User();
            admin.setFirstName(this.firstName);
            admin.setLastName(this.lastName);
            admin.setEmail(this.email);
            admin.setPassword(this.passwordEncoder.encode(this.password));

            Role role = this.roleService.findByName(RoleName.ADMIN).orElseThrow(NoSuchElementException::new);

            admin.setRoles(Collections.singleton(role));

            this.userService.register(admin);
        }
    }
}
