package primeholding.rushhour.models;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import primeholding.rushhour.entities.Activity;
import primeholding.rushhour.entities.Appointment;
import primeholding.rushhour.entities.User;
import primeholding.rushhour.models.activities.GetActivityModel;
import primeholding.rushhour.models.activities.PostActivityModel;
import primeholding.rushhour.models.activities.PutActivityModel;
import primeholding.rushhour.models.appointments.GetAppointmentModel;
import primeholding.rushhour.models.appointments.PostAppointmentModel;
import primeholding.rushhour.models.users.GetUserModel;
import primeholding.rushhour.models.users.PutUserModel;
import primeholding.rushhour.models.users.PostUserModel;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ModelMapper {
    User registerToUser(PostUserModel postUserModel);

    GetUserModel userToGetModel(User user);

    User putModelToUser(PutUserModel getUserModel);

    GetActivityModel activityToGetModel(Activity activity);

    Activity getModelToActivity(GetActivityModel getActivityModel);

    Activity postModelToActivity(PostActivityModel postActivityModel);

    Activity putModelToActivity(PutActivityModel putActivityModel);

    GetAppointmentModel appointmentToGetModel(Appointment appointment);

    Appointment postModelToAppointment(PostAppointmentModel postAppointmentModel);
}
