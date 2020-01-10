package primeholding.rushhour.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import primeholding.rushhour.entities.User;
import primeholding.rushhour.repositories.UserRepository;
import primeholding.rushhour.security.UserPrincipal;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class UserService implements BaseService<User>, UserDetailsService {

    private static final Logger LOGGER = Logger.getLogger(UserService.class.getName());

    private UserRepository repository;

    @Autowired
    public UserService(UserRepository repository) {
        this.repository = repository;
    }

    @Override
    public User register(User entity) {
        return this.repository.save(entity);
    }

    @Override
    public List<User> findAll() {
        return this.repository.findAll();
    }

    @Override
    public Optional<User> findById(Long id) {
        return this.repository.findById(id);
    }

    @Override
    public UserDetails loadUserByUsername(String email) {
        Optional<User> userByEmail = this.repository.getUserByEmail(email);
        if (!userByEmail.isPresent()) {
            throw new NoSuchElementException();
        }

        return UserPrincipal.create(userByEmail.get());
    }

    @Override
    public void deleteById(Long id){
        this.repository.deleteById(id);
    }

    public User getUser(Long id){
        return this.repository.getOne(id);
    }

    public UserDetails loadUserById(Long userId) {
        Optional<User> optionalUser = this.repository.findById(userId);
        if (!optionalUser.isPresent()) {
            throw new NoSuchElementException();
        }

        return UserPrincipal.create(optionalUser.get());
    }

    public Optional<User> findByEmail(String email){
        return this.repository.findByEmail(email);
    }

    public boolean existWithEmail(String email) {
        return this.repository.existsByEmail(email);
    }

    public User update(User user, Map<String, Object> fields) {
        for (Map.Entry<String, Object> stringObjectEntry : fields.entrySet()) {
            Field entityFiled;
            try {
                entityFiled = user.getClass().getDeclaredField(stringObjectEntry.getKey());
                entityFiled.setAccessible(true);
                if(stringObjectEntry.getKey().equals("password")){
                    entityFiled.set(user,stringObjectEntry.getValue().toString());
                    continue;
                }
                entityFiled.set(user, stringObjectEntry.getValue());
            } catch (NoSuchFieldException | IllegalAccessException e) {
                LOGGER.log(Level.SEVERE, e.getMessage(), e);
            }
        }
        return user;
    }
}
