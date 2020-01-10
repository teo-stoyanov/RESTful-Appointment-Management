package primeholding.rushhour.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import primeholding.rushhour.entities.Appointment;

import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    @Query(value = "SELECT activity_id FROM activity_appointment WHERE appointment_id = ?1", nativeQuery = true)
    List<Long> getActivitiesByAppointmentId(Long id);

    List<Appointment> getAppointmentsByUserId(Long id);
}
