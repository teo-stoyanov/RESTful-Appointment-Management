package primeholding.rushhour.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import primeholding.rushhour.entities.Activity;
import primeholding.rushhour.repositories.ActivityRepository;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class ActivityService implements BaseService<Activity> {
    private static final Logger LOGGER = Logger.getLogger(ActivityService.class.getName());

    private ActivityRepository repository;

    @Autowired
    public ActivityService(ActivityRepository repository) {
        this.repository = repository;
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
    public void delete(Activity activity) {
        this.repository.delete(activity);
    }

    public boolean existWithName(String name) {
        return this.repository.existsByName(name);
    }

    public Activity update(Activity activity, Map<String, Object> fields) {
        for (Map.Entry<String, Object> stringObjectEntry : fields.entrySet()) {
            Field entityFiled;
            try {
                entityFiled = activity.getClass().getDeclaredField(stringObjectEntry.getKey());
                entityFiled.setAccessible(true);
                if (stringObjectEntry.getKey().equals("price")) {
                    BigDecimal temp;
                    temp = getBigDecimal(stringObjectEntry);
                    entityFiled.set(activity, temp);
                    continue;
                }
                entityFiled.set(activity, stringObjectEntry.getValue());
            } catch (NoSuchFieldException | IllegalAccessException e) {
                LOGGER.log(Level.SEVERE, e.getMessage(), e);
            }
        }
        return activity;
    }

    public Optional<Activity> findByName(String name) {
        return this.repository.findByName(name);
    }

    private BigDecimal getBigDecimal(Map.Entry<String, Object> stringObjectEntry) {
        BigDecimal temp;
        try {
            temp = BigDecimal.valueOf((Double) stringObjectEntry.getValue());
        } catch (Exception ex) {
            temp = BigDecimal.valueOf((Integer) stringObjectEntry.getValue());
        }
        return temp;
    }
}
