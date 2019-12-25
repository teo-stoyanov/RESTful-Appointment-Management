package primeholding.rushhour.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import primeholding.rushhour.entities.Role;
import primeholding.rushhour.entities.RoleName;
import primeholding.rushhour.repositories.RoleRepository;

import java.util.Optional;

@Service
public class RoleService implements BaseService<Role>{

    private RoleRepository roleRepository;

    @Autowired
    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public Role register(Role entity) {
        return this.roleRepository.save(entity);
    }

    public Optional<Role> findByName(RoleName roleName){
        return this.roleRepository.findByName(roleName);
    }

    public Role getByName(RoleName name){
        return this.roleRepository.getByName(name);
    }
}
