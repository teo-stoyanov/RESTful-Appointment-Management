package primeholding.rushhour.models;

import org.mapstruct.Mapper;
import primeholding.rushhour.entities.User;

@Mapper
public interface ModelMapper {
    User registerToUser(RegisterModel registerModel);
}
