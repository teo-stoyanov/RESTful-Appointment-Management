package primeholding.rushhour.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import primeholding.rushhour.entities.Activity;
import primeholding.rushhour.entities.Appointment;
import primeholding.rushhour.repositories.AppointmentRepository;
import org.springframework.data.domain.Pageable;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Service
public class AppointmentService implements BaseService<Appointment> {
    private static final Logger LOGGER = Logger.getLogger(AppointmentService.class.getName());

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
    public List<Appointment> findAll(Pageable pageable) {
       return this.repository.findAll(pageable).toList();
    }

    @Override
    public Optional<Appointment> findById(Long id) {
        return this.repository.findById(id);
    }

    @Override
    public void deleteById(Long id) {
        this.repository.deleteById(id);
    }

    @Override
    public Appointment update(Appointment entity, Map<String, Object> fields) {
        fields.forEach((key, value) -> {
            Field entityFiled;
            try {
                entityFiled = entity.getClass().getDeclaredField(key);
                entityFiled.setAccessible(true);
                if (key.equals("startDate")) {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                    LocalDateTime dateTime = LocalDateTime.parse(value.toString(), formatter);
                    entityFiled.set(entity,dateTime);
                } else {
                    entityFiled.set(entity, value);
                }
            } catch (NoSuchFieldException | IllegalAccessException e) {
                LOGGER.log(Level.SEVERE, e.getMessage(), e);
            }
        });

        return entity;
    }

    @Override
    public Appointment getEntity(Long id) {
        return this.repository.getOne(id);
    }

    public List<Appointment> findAll(){
        return this.repository.findAll();
    }

    public List<Long> getActivitiesByAppointmentId(Long id) {
        return this.repository.getActivitiesByAppointmentId(id);
    }

    public List<Appointment> getAppointmentsByUserId(Long id) {
        return this.repository.getAppointmentsByUserId(id);
    }

    public boolean isAppointmentTimeAvailable(LocalDateTime startDate, List<Long> activityIds) {
        if(startDate.isBefore(LocalDateTime.now())){
            return false;
        }

        List<Activity> activities = activityIds
                .stream()
                .map(x -> this.activityService.getEntity(x))
                .collect(Collectors.toList());

        LocalDateTime endDate = startDate;
        List<Appointment> appointments = new ArrayList<>();
        for (Activity activity : activities) {
            endDate = endDate.plusMinutes(activity.getMinDuration());
            List<Long> appointmentsByActivityId = this.activityService.getAppointmentsByActivityId(activity.getId());
            appointmentsByActivityId
                    .forEach(x -> appointments.add(this.getEntity(x)));
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
                .map(x -> this.activityService.getEntity(x))
                .collect(Collectors.toList());

        for (Activity activity : activities) {
            startDate = startDate.plusMinutes(activity.getMinDuration());
        }

        return startDate;
    }
}
