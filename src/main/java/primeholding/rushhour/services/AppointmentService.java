package primeholding.rushhour.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import primeholding.rushhour.entities.Appointment;
import primeholding.rushhour.repositories.AppointmentRepository;

import java.util.List;
import java.util.Optional;

@Service
public class AppointmentService implements BaseService<Appointment> {

    private AppointmentRepository repository;

    @Autowired
    public AppointmentService(AppointmentRepository repository) {
        this.repository = repository;
    }

    @Override
    public Appointment register(Appointment entity) {
        return this.repository.save(entity);
    }

    @Override
    public List<Appointment> findAll() {
        return this.repository.findAll();
    }

    @Override
    public Optional<Appointment> findById(Long id) {
        return this.repository.findById(id);
    }

    @Override
    public void delete(Appointment appointment) {
        this.repository.delete(appointment);
    }
}
