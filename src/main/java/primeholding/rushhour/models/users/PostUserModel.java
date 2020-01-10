package primeholding.rushhour.models.users;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
public class PostUserModel {
    @NotBlank(message = "First Name should not be blank")
    @Size(max = 40)
    private String firstName;

    @NotBlank(message = "Last Name should not be blank")
    @Size(max = 40)
    private String lastName;

    @NotBlank(message = "Email should not be blank")
    @Email(regexp = "^([\\w-\\.]+){1,64}@([\\w&&[^_]]+){2,255}.[a-z]{2,}$")
    @Size(max = 100)
    private String email;

    @NotBlank(message = "Password should not be blank")
    @Size(max = 100)
    private String password;
}
