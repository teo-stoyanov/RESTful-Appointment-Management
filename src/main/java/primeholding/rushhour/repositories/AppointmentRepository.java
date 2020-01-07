package primeholding.rushhour.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import primeholding.rushhour.entities.Appointment;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
}
