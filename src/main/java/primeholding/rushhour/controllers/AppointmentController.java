package primeholding.rushhour.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import primeholding.rushhour.entities.Activity;
import primeholding.rushhour.entities.Appointment;
import primeholding.rushhour.entities.User;
import primeholding.rushhour.models.ModelMapper;
import primeholding.rushhour.models.activities.GetActivityModel;
import primeholding.rushhour.models.appointments.GetAppointmentModel;
import primeholding.rushhour.models.appointments.PostAppointmentModel;
import primeholding.rushhour.responses.ErrorResponse;
import primeholding.rushhour.responses.Response;
import primeholding.rushhour.services.ActivityService;
import primeholding.rushhour.services.AppointmentService;
import primeholding.rushhour.services.UserService;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/appointments")
public class AppointmentController extends BaseController {

    private AppointmentService appointmentService;

    private UserService userService;

    private ActivityService activityService;

    private ModelMapper mapper;

    @Autowired
    public AppointmentController(AppointmentService appointmentService, UserService userService, ActivityService activityService
            , ModelMapper mapper) {
        this.appointmentService = appointmentService;
        this.userService = userService;
        this.activityService = activityService;
        this.mapper = mapper;
    }

    @GetMapping
    public ResponseEntity<List<GetAppointmentModel>> get() {
        List<Appointment> appointmentList = this.appointmentService.findAll();
        List<GetAppointmentModel> getAppointmentModels = appointmentList
                .stream()
                .map(x -> this.mapper.appointmentToGetModel(x))
                .collect(Collectors.toList());

        return new ResponseEntity<>(getAppointmentModels, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<GetAppointmentModel> get(@PathVariable Long id) {
        Optional<Appointment> optionalAppointment = this.appointmentService.findById(id);
        if (!optionalAppointment.isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        GetAppointmentModel getAppointmentModel = this.mapper.appointmentToGetModel(optionalAppointment.get());

        return new ResponseEntity<>(getAppointmentModel, HttpStatus.OK);
    }

    @GetMapping("/{id}/activities")
    public ResponseEntity<Set<GetActivityModel>> getAppointments(@PathVariable Long id) {
        Optional<Appointment> optionalAppointment = this.appointmentService.findById(id);
        if (!optionalAppointment.isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        List<Long> activityIds = this.appointmentService.getActivitiesByAppointmentId(id);
        Set<GetActivityModel> getActivityModels = new HashSet<>();

        for (Long activityId : activityIds) {
            Activity activity = this.activityService.getActivity(activityId);
            GetActivityModel getActivityModel = this.mapper.activityToGetModel(activity);
            getActivityModels.add(getActivityModel);
        }

        return new ResponseEntity<>(getActivityModels, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<Response> post(@RequestBody @Valid PostAppointmentModel postAppointmentModel) {
        Optional<User> optionalUser = this.userService.findById(postAppointmentModel.getUserId());
        if (!optionalUser.isPresent()) {
            return new ResponseEntity<>(new ErrorResponse(HttpStatus.NOT_FOUND, "User not found", "No such user")
                    , HttpStatus.NOT_FOUND);
        }
        if (!this.activityService.isValidActivity(postAppointmentModel.getActivityIds())) {
            return new ResponseEntity<>(new ErrorResponse(HttpStatus.NOT_FOUND, "Activity not found", "No such activity"),
                    HttpStatus.NOT_FOUND);
        }
        if (!this.appointmentService.isAppointmentTimeAvailable(postAppointmentModel)) {
            return new ResponseEntity<>(new ErrorResponse(HttpStatus.BAD_REQUEST, "Choose different start time",
                    "Time conflict"), HttpStatus.BAD_REQUEST);
        }

        Appointment appointment = this.mapper.postModelToAppointment(postAppointmentModel);
        LocalDateTime appointmentEndDate = this.appointmentService.getAppointmentEndDate(appointment.getStartDate()
                , postAppointmentModel.getActivityIds());
        appointment.setEndDate(appointmentEndDate);
        appointment.setUser(this.userService.getUser(postAppointmentModel.getUserId()));
        this.activityService.setActivities(appointment, postAppointmentModel.getActivityIds());
        this.appointmentService.register(appointment);

        return super.successResponse("Updated");
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Response> patch(@PathVariable Long id, @RequestBody Map<String, Object> fields) {
        Optional<Appointment> optionalAppointment = this.appointmentService.findById(id);
        if (!optionalAppointment.isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        if (fields.containsKey("activityIds")) {
            List<Long> activityIds = ((List<Integer>) fields.get("activityIds"))
                    .stream()
                    .map(Long::valueOf)
                    .collect(Collectors.toList());
            if (!this.activityService.isValidActivity(activityIds)) {
                return new ResponseEntity<>(new ErrorResponse(HttpStatus.NOT_FOUND, "Activity not found", "No such activity"),
                        HttpStatus.NOT_FOUND);
            }
        }

        //TODO:

        return null;
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Response> delete(@PathVariable Long id) {
        Optional<Appointment> optionalAppointment = this.appointmentService.findById(id);
        if (!optionalAppointment.isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        this.appointmentService.deleteById(id);

        return super.successResponse("Deleted");
    }
}
