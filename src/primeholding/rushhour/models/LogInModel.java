package primeholding.rushhour.models;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Data
public class LogInModel {
    @NotBlank
    @Email(regexp = "^([\\w-\\.]+){1,64}@([\\w&&[^_]]+){2,255}.[a-z]{2,}$")
    private String email;

    @NotBlank
    private String password;
}
