package primeholding.rushhour.services;

import java.util.List;
import java.util.Optional;

public interface BaseService<T> {

    T register(T entity);

    List<T> findAll();

    Optional<T> findById(Long id);

    void deleteById(Long id);
}
