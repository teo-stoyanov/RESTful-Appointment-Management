package primeholding.rushhour.models.activities;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class GetActivityModel {

    private LocalDateTime createdDate;
    private LocalDateTime lastModifiedDate;
    private Long id;
    private Integer minDuration;
    private BigDecimal price;
}
