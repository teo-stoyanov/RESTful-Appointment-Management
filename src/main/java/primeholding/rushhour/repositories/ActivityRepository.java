package primeholding.rushhour.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import primeholding.rushhour.entities.Activity;

import java.util.List;
import java.util.Optional;

@Repository
public interface ActivityRepository extends JpaRepository<Activity, Long> {

    List<Activity> findAll();

    boolean existsByName(String name);

    Optional<Activity> findByName(String name);

    @Query(value = "SELECT appointment_id FROM activity_appointment WHERE activity_id = ?1", nativeQuery = true)
    List<Long> getAppointmentsByActivityId(Long id);
}
