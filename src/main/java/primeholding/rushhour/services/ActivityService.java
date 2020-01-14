package primeholding.rushhour.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import primeholding.rushhour.entities.Activity;
import primeholding.rushhour.entities.Appointment;
import primeholding.rushhour.models.ModelMapper;
import primeholding.rushhour.models.activities.GetActivityModel;
import primeholding.rushhour.repositories.ActivityRepository;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class ActivityService implements BaseService<Activity> {
    private static final Logger LOGGER = Logger.getLogger(ActivityService.class.getName());

    private ActivityRepository repository;

    private ModelMapper mapper;

    @Autowired
    public ActivityService(ActivityRepository repository, ModelMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Activity register(Activity entity) {
        return this.repository.save(entity);
    }

    @Override
    public List<Activity> findAll() {
        return this.repository.findAll();
    }

    @Override
    public Optional<Activity> findById(Long id) {
        return this.repository.findById(id);
    }

    @Override
    public void deleteById(Long id) {
        this.repository.deleteById(id);
    }

    @Override
    public Activity update(Activity activity, Map<String, Object> fields) {
        fields.forEach((key, value) -> {
            Field entityFiled;
            try {
                entityFiled = activity.getClass().getDeclaredField(key);
                entityFiled.setAccessible(true);
                if (key.equals("price")) {
                    BigDecimal temp = getBigDecimal(value);
                    entityFiled.set(activity, temp);
                } else {
                    entityFiled.set(activity, value);
                }
            } catch (NoSuchFieldException | IllegalAccessException e) {
                LOGGER.log(Level.SEVERE, e.getMessage(), e);
            }
        });

        return activity;
    }

    @Override
    public Activity getEntity(Long id){
        return this.repository.getOne(id);
    }

    public List<Long> getAppointmentsByActivityId(Long id){
        return this.repository.getAppointmentsByActivityId(id);
    }

    public boolean existWithName(String name) {
        return this.repository.existsByName(name);
    }

    public Optional<Activity> findByName(String name) {
        return this.repository.findByName(name);
    }

    private BigDecimal getBigDecimal(Object value) {
        BigDecimal temp;
        try {
            temp = BigDecimal.valueOf((Double) value);
        } catch (Exception ex) {
            temp = BigDecimal.valueOf((Integer) value);
        }
        return temp;
    }

    public boolean isValidActivity(List<Long> activityIds) {
        if(activityIds.isEmpty()){
            return false;
        }

        for (Long activityId : activityIds) {
            Optional<Activity> optionalActivity = this.repository.findById(activityId);
            if (!optionalActivity.isPresent()) {
                return false;
            }
        }
        return true;
    }

    public void setActivities(Appointment appointment, List<Long> activityIds){
        Set<Long> longSet = new HashSet<>(activityIds);
        for (Long activityId : longSet) {
            GetActivityModel getActivityModel = this.mapper.activityToGetModel(this.getEntity(activityId));
            Activity activity = this.mapper.getModelToActivity(getActivityModel);
            appointment.getActivities().add(activity);
        }
    }
}
