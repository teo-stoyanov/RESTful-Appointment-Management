package primeholding.rushhour.services;


import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface BaseService<T> {

    T register(T entity);

    List<T> findAll(Pageable pageable);

    Optional<T> findById(Long id);

    void deleteById(Long id);

    T update(T entity, Map<String, Object> fields);

    T getEntity(Long id);
}
