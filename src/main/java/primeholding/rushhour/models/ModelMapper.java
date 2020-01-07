package primeholding.rushhour.models;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import primeholding.rushhour.entities.Activity;
import primeholding.rushhour.entities.User;
import primeholding.rushhour.models.activities.GetActivityModel;
import primeholding.rushhour.models.activities.PostActivityModel;
import primeholding.rushhour.models.activities.PutActivityModel;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ModelMapper {
    User registerToUser(RegisterModel registerModel);

    GetActivityModel activityToGetModel(Activity activity);

    Activity postModelToActivity(PostActivityModel postActivityModel);

    Activity putModelToActivity(PutActivityModel putActivityModel);
}
