package primeholding.rushhour.models.appointments;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class GetAppointmentModel {

    private LocalDateTime createdDate;

    private LocalDateTime lastModifiedDate;

    private Long id;

    private LocalDateTime startDate;

    private LocalDateTime endDate;
}
