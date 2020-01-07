package primeholding.rushhour.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import primeholding.rushhour.entities.User;
import primeholding.rushhour.repositories.UserRepository;
import primeholding.rushhour.security.UserPrincipal;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class UserService implements BaseService<User>, UserDetailsService {

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
    public void delete(User user) {
        this.repository.delete(user);
    }

    @Override
    public UserDetails loadUserByUsername(String email) {
        Optional<User> userByEmail = this.repository.getUserByEmail(email);
        if (!userByEmail.isPresent()) {
            throw new NoSuchElementException();
        }

        return UserPrincipal.create(userByEmail.get());
    }

    public UserDetails loadUserById(Long userId) {
        Optional<User> optionalUser = this.repository.findById(userId);
        if (!optionalUser.isPresent()) {
            throw new NoSuchElementException();
        }

        return UserPrincipal.create(optionalUser.get());
    }

    public boolean existWithEmail(String email) {
        return this.repository.existsByEmail(email);
    }
}
