package primeholding.rushhour.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
public class AppointmentController extends BaseController {
    private static final String ACTIVITY_IDS = "activityIds";
    private static final String START_DATE = "startDate";
    private static final String ACTIVITY_NOT_FOUND = "Activity not found";
    private static final String NO_SUCH_ACTIVITY = "No such activity";
    private static final String CHOOSE_DIFFERENT_START_TIME = "No available appointment at this time";
    private static final String TIME_CONFLICT = "Time conflict";
    private static final String USER_NOT_FOUND = "User not found";
    private static final String NO_SUCH_USER = "No such user";

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

    @GetMapping("/appointments")
    public ResponseEntity<PageImpl> get(Pageable pageable) {

        List<GetAppointmentModel> getAppointmentModels = this.appointmentService
                .findAll(pageable)
                .stream()
                .map(x -> {
                    GetAppointmentModel model = this.mapper.appointmentToGetModel(x);
                    model.setUserId(x.getUser().getId());
                    return model;
                })
                .collect(Collectors.toList());

        return new ResponseEntity<>(new PageImpl(getAppointmentModels,pageable,getAppointmentModels.size()), HttpStatus.OK);
    }

    @GetMapping("/appointments/{id}")
    public ResponseEntity<GetAppointmentModel> get(@PathVariable Long id) {
        Optional<Appointment> optionalAppointment = this.appointmentService.findById(id);
        if (!optionalAppointment.isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        GetAppointmentModel getAppointmentModel = this.mapper.appointmentToGetModel(optionalAppointment.get());
        getAppointmentModel.setUserId(optionalAppointment.get().getUser().getId());

        return new ResponseEntity<>(getAppointmentModel, HttpStatus.OK);
    }

    @GetMapping("/appointments/{id}/activities")
    public ResponseEntity<Set<GetActivityModel>> getAppointments(@PathVariable Long id) {
        Optional<Appointment> optionalAppointment = this.appointmentService.findById(id);
        if (!optionalAppointment.isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        Set<GetActivityModel> getActivityModels = this.appointmentService.getActivitiesByAppointmentId(id)
                .stream()
                .map(x -> {
                    Activity activity = this.activityService.getEntity(x);
                    return this.mapper.activityToGetModel(activity);
                }).collect(Collectors.toSet());

        return new ResponseEntity<>(getActivityModels, HttpStatus.OK);
    }

    @PostMapping("user/{id}/appointments")
    public ResponseEntity<?> post(@PathVariable Long id, @RequestBody @Valid PostAppointmentModel postAppointmentModel) {
        Optional<User> optionalUser = this.userService.findById(id);
        if (!optionalUser.isPresent()) {
            return new ResponseEntity<>(new ErrorResponse(HttpStatus.NOT_FOUND, USER_NOT_FOUND, NO_SUCH_USER)
                    , HttpStatus.NOT_FOUND);
        }
        if (!this.activityService.isValidActivity(postAppointmentModel.getActivityIds())) {
            return new ResponseEntity<>(new ErrorResponse(HttpStatus.NOT_FOUND, ACTIVITY_NOT_FOUND, NO_SUCH_ACTIVITY),
                    HttpStatus.NOT_FOUND);
        }
        if (!this.appointmentService.isAppointmentTimeAvailable(postAppointmentModel.getStartDate(), postAppointmentModel.getActivityIds())) {
            return new ResponseEntity<>(new ErrorResponse(HttpStatus.BAD_REQUEST, CHOOSE_DIFFERENT_START_TIME,
                    TIME_CONFLICT), HttpStatus.BAD_REQUEST);
        }

        Appointment appointment = this.mapper.postModelToAppointment(postAppointmentModel);
        LocalDateTime appointmentEndDate = this.appointmentService.getAppointmentEndDate(appointment.getStartDate()
                , postAppointmentModel.getActivityIds());
        appointment.setEndDate(appointmentEndDate);
        appointment.setUser(this.userService.getEntity(id));
        this.activityService.setActivities(appointment, postAppointmentModel.getActivityIds());
        Appointment register = this.appointmentService.register(appointment);
        GetAppointmentModel model = this.mapper.appointmentToGetModel(register);
        model.setUserId(register.getUser().getId());

        return new ResponseEntity<>(model, HttpStatus.CREATED);
    }

    @PatchMapping("/appointments/{id}")
    public ResponseEntity<Response> patch(@PathVariable Long id, @RequestBody Map<String, Object> fields) {
        Optional<Appointment> optionalAppointment = this.appointmentService.findById(id);
        if (!optionalAppointment.isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        ResponseEntity<Response> errorResponse = checkForErrorResponse(id, fields, optionalAppointment.get());
        if (errorResponse != null) return errorResponse;

        Appointment appointment = this.appointmentService.update(optionalAppointment.get(), fields);

        try {
            this.appointmentService.register(appointment);
        } catch (TransactionSystemException e) {
            super.constraintViolationCheck(e);
        }

        return super.successResponse("Updated");
    }

    @DeleteMapping("/appointments/{id}")
    public ResponseEntity<Response> delete(@PathVariable Long id) {
        Optional<Appointment> optionalAppointment = this.appointmentService.findById(id);
        if (!optionalAppointment.isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        this.appointmentService.deleteById(id);

        return super.successResponse("Deleted");
    }

    private ResponseEntity<Response> checkForErrorResponse(Long id, Map<String, Object> fields, Appointment appointment) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        if (fields.containsKey(ACTIVITY_IDS) && !fields.containsKey(START_DATE)) {
            List<Long> activityIds = ((List<Integer>) fields.get(ACTIVITY_IDS))
                    .stream()
                    .map(Long::valueOf)
                    .collect(Collectors.toList());

            if (!this.activityService.isValidActivity(activityIds)) {
                return new ResponseEntity<>(new ErrorResponse(HttpStatus.NOT_FOUND, ACTIVITY_NOT_FOUND, NO_SUCH_ACTIVITY),
                        HttpStatus.NOT_FOUND);
            } else if (!this.appointmentService.isAppointmentTimeAvailable(appointment.getStartDate(), activityIds)) {
                return new ResponseEntity<>(new ErrorResponse(HttpStatus.BAD_REQUEST, CHOOSE_DIFFERENT_START_TIME,
                        TIME_CONFLICT), HttpStatus.BAD_REQUEST);
            } else {
                LocalDateTime appointmentEndDate = this.appointmentService.getAppointmentEndDate(appointment.getStartDate(), activityIds);
                appointment.setEndDate(appointmentEndDate);
            }
        } else if (!fields.containsKey(ACTIVITY_IDS) && fields.containsKey(START_DATE)) {
            List<Long> activityIds = this.appointmentService.getActivitiesByAppointmentId(id);
            LocalDateTime dateTime = LocalDateTime.parse(fields.get(START_DATE).toString(), formatter);
            if (!this.appointmentService.isAppointmentTimeAvailable(dateTime, activityIds)) {
                return new ResponseEntity<>(new ErrorResponse(HttpStatus.BAD_REQUEST, CHOOSE_DIFFERENT_START_TIME,
                        TIME_CONFLICT), HttpStatus.BAD_REQUEST);
            } else {
                LocalDateTime appointmentEndDate = this.appointmentService.getAppointmentEndDate(dateTime, activityIds);
                appointment.setEndDate(appointmentEndDate);
            }
        } else if (fields.containsKey(ACTIVITY_IDS) && fields.containsKey(START_DATE)) {
            List<Long> activityIds = ((List<Integer>) fields.get(ACTIVITY_IDS))
                    .stream()
                    .map(Long::valueOf)
                    .collect(Collectors.toList());

            LocalDateTime dateTime = LocalDateTime.parse(fields.get(START_DATE).toString(), formatter);
            if (!this.activityService.isValidActivity(activityIds)) {
                return new ResponseEntity<>(new ErrorResponse(HttpStatus.NOT_FOUND, ACTIVITY_NOT_FOUND, NO_SUCH_ACTIVITY),
                        HttpStatus.NOT_FOUND);
            } else if (!this.appointmentService.isAppointmentTimeAvailable(dateTime, activityIds)) {
                return new ResponseEntity<>(new ErrorResponse(HttpStatus.BAD_REQUEST, CHOOSE_DIFFERENT_START_TIME,
                        TIME_CONFLICT), HttpStatus.BAD_REQUEST);
            } else {
                LocalDateTime appointmentEndDate = this.appointmentService.getAppointmentEndDate(dateTime, activityIds);
                appointment.setEndDate(appointmentEndDate);
            }
        }
        return null;
    }
}
