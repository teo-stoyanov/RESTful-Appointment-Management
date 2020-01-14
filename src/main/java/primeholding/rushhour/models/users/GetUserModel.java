package primeholding.rushhour.models.users;

import lombok.Data;
import primeholding.rushhour.entities.Role;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Data
public class GetUserModel {

    private LocalDateTime createdDate;

    private LocalDateTime lastModifiedDate;

    private Long id;

    private String firstName;

    private String lastName;

    private String email;

    private Set<Role> role = new HashSet<>();
}
