package primeholding.rushhour.models.activities;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;

@Data
public class PutActivityModel {
    @NotBlank
    @Size(max = 40)
    private String name;

    @Min(1)
    @NotNull
    private Integer minDuration;

    @Min(1)
    @NotNull
    private BigDecimal price;

    @Min(1)
    private Long appointmentId;
}
