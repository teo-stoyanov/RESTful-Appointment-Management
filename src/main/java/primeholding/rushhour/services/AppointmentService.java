package primeholding.rushhour.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import primeholding.rushhour.entities.Activity;
import primeholding.rushhour.entities.Appointment;
import primeholding.rushhour.models.appointments.PostAppointmentModel;
import primeholding.rushhour.repositories.AppointmentRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AppointmentService implements BaseService<Appointment> {

    private ActivityService activityService;

    private AppointmentRepository repository;

    @Autowired
    public AppointmentService(ActivityService activityService, AppointmentRepository repository) {
        this.activityService = activityService;
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
    public void deleteById(Long id) {
        this.repository.deleteById(id);
    }

    public Appointment getAppointment(Long id) {
        return this.repository.getOne(id);
    }

    public List<Long> getActivitiesByAppointmentId(Long id) {
        return this.repository.getActivitiesByAppointmentId(id);
    }

    public List<Appointment> getAppointmentsByUserId(Long id) {
        return this.repository.getAppointmentsByUserId(id);
    }

    public boolean isAppointmentTimeAvailable(PostAppointmentModel postAppointmentModel) {

        LocalDateTime startDate = postAppointmentModel.getStartDate();
        if(startDate.isBefore(LocalDateTime.now())){
            return false;
        }

        List<Activity> activities = postAppointmentModel
                .getActivityIds()
                .stream()
                .map(x -> this.activityService.getActivity(x))
                .collect(Collectors.toList());

        LocalDateTime endDate = startDate;
        List<Appointment> appointments = new ArrayList<>();
        for (Activity activity : activities) {
            endDate = endDate.plusMinutes(activity.getMinDuration());
            List<Long> appointmentsByActivityId = this.activityService.getAppointmentsByActivityId(activity.getId());
            appointmentsByActivityId
                    .forEach(x -> appointments.add(this.getAppointment(x)));
        }

        for (Appointment appointment : appointments) {
            if (!appointment.getEndDate().isBefore(startDate) && !appointment.getEndDate().isEqual(startDate)) {
                if(!endDate.isBefore(appointment.getStartDate()) && !endDate.isEqual(appointment.getStartDate())){
                    return false;
                }
            }
        }

        return true;
    }

    public LocalDateTime getAppointmentEndDate(LocalDateTime startDate, List<Long> activityIds){
        List<Activity> activities =activityIds
                .stream()
                .map(x -> this.activityService.getActivity(x))
                .collect(Collectors.toList());

        for (Activity activity : activities) {
            startDate = startDate.plusMinutes(activity.getMinDuration());
        }

        return startDate;
    }
}
