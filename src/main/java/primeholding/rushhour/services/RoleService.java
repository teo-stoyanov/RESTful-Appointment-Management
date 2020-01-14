package primeholding.rushhour.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import primeholding.rushhour.entities.Role;
import primeholding.rushhour.entities.RoleName;
import primeholding.rushhour.repositories.RoleRepository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class RoleService implements BaseService<Role> {

    private RoleRepository repository;

    @Autowired
    public RoleService(RoleRepository repository) {
        this.repository = repository;
    }

    @Override
    public Role register(Role entity) {
        return this.repository.save(entity);
    }

    @Override
    public List<Role> findAll() {
        return this.repository.findAll();
    }

    @Override
    public Optional<Role> findById(Long id) {
        return this.repository.findById(id);
    }

    @Override
    public void deleteById(Long id) {
        this.repository.deleteById(id);
    }

    @Override
    public Role update(Role entity, Map<String, Object> fields) {
        return null;
    }

    @Override
    public Role getEntity(Long id) {
        return this.repository.getOne(id);
    }

    public Optional<Role> findByName(RoleName roleName) {
        return this.repository.findByName(roleName);
    }

    public Role getByName(RoleName name) {
        return this.repository.getByName(name);
    }
}
