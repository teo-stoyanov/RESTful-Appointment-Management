package primeholding.rushhour.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import primeholding.rushhour.entities.Activity;
import primeholding.rushhour.entities.Appointment;
import primeholding.rushhour.models.ModelMapper;
import primeholding.rushhour.models.activities.GetActivityModel;
import primeholding.rushhour.models.activities.PostActivityModel;
import primeholding.rushhour.models.activities.PutActivityModel;
import primeholding.rushhour.responses.ErrorResponse;
import primeholding.rushhour.responses.Response;
import primeholding.rushhour.responses.SuccessResponse;
import primeholding.rushhour.services.ActivityService;
import primeholding.rushhour.services.AppointmentService;
import primeholding.rushhour.services.BaseService;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/activities")
public class ActivityController extends BaseController {
    private static final String NO_SUCH_ACTIVITY = "No such activity!";
    private static final String ACTIVITY_NOT_FOUND = "Activity not found!";
    private static final String MIN_DURATION = "minDuration";
    private static final String PRICE = "price";

    private ActivityService activityService;

    private AppointmentService appointmentService;

    private ModelMapper mapper;

    @Autowired
    public ActivityController(ActivityService activityService, AppointmentService appointmentService, ModelMapper mapper) {
        this.activityService = activityService;
        this.appointmentService = appointmentService;
        this.mapper = mapper;
    }

    @GetMapping
    public ResponseEntity<List<GetActivityModel>> get() {
        List<Activity> activities = this.activityService.findAll();
        List<GetActivityModel> getActivityModels = activities
                .stream()
                .map(x -> this.mapper.activityToGetModel(x))
                .collect(Collectors.toList());

        return new ResponseEntity<>(getActivityModels, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<GetActivityModel> get(@PathVariable Long id) {
        Optional<Activity> optionalActivity = this.activityService.findById(id);
        if (!optionalActivity.isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        GetActivityModel getActivityModel = this.mapper.activityToGetModel(optionalActivity.get());

        return new ResponseEntity<>(getActivityModel, HttpStatus.OK);
    }

    @GetMapping("/{id}/appointments")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Set<Appointment>> getAppointments(@PathVariable Long id) {
        Optional<Activity> optionalActivity = this.activityService.findById(id);
        if (!optionalActivity.isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        Set<Appointment> appointments = optionalActivity.get().getAppointments();

        return new ResponseEntity<>(appointments, HttpStatus.OK);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Response> post(@RequestBody @Valid PostActivityModel postActivityModel) {
        if (this.activityService.existWithName(postActivityModel.getName())) {
            return new ResponseEntity<>(new ErrorResponse(HttpStatus.BAD_REQUEST, "Name exists!", "Name already in use!"),
                    HttpStatus.BAD_REQUEST);
        }

        Activity activity = this.mapper.postModelToActivity(postActivityModel);

        if (!isValidAppointment(activity, postActivityModel.getAppointmentId(), this.appointmentService)) {
            return new ResponseEntity<>(new ErrorResponse(HttpStatus.BAD_REQUEST, "Appointment not found!", "Appointment not exists!")
                    , HttpStatus.BAD_REQUEST);
        }

        this.activityService.register(activity);

        return super.successResponse();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Response> put(@PathVariable Long id, @RequestBody @Valid PutActivityModel putActivityModel) {
        Optional<Activity> optionalActivity = this.activityService.findById(id);
        if (!optionalActivity.isPresent()) {
            return new ResponseEntity<>(new ErrorResponse(HttpStatus.NOT_FOUND, NO_SUCH_ACTIVITY, ACTIVITY_NOT_FOUND), HttpStatus.NOT_FOUND);
        }

        Optional<Activity> optional = this.activityService.findByName(putActivityModel.getName());
        if (optional.isPresent() && !optional.get().getId().equals(id)) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }

        Activity activity = this.mapper.putModelToActivity(putActivityModel);

        if (!isValidAppointment(activity, putActivityModel.getAppointmentId(), this.appointmentService)) {
            return new ResponseEntity<>(new ErrorResponse(HttpStatus.BAD_REQUEST, "Appointment not found!", "Appointment not exists!")
                    , HttpStatus.BAD_REQUEST);
        }
        activity.setId(id);
        this.activityService.register(activity);

        return super.successResponse();
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Response> patch(@PathVariable Long id, @RequestBody Map<String, Object> fields) {
        Optional<Activity> optionalActivity = this.activityService.findById(id);
        if (!optionalActivity.isPresent()) {
            return new ResponseEntity<>(new ErrorResponse(HttpStatus.NOT_FOUND, NO_SUCH_ACTIVITY, ACTIVITY_NOT_FOUND), HttpStatus.NOT_FOUND);
        }

        Optional<Activity> optional = this.activityService.findByName(fields.get("name").toString());
        if (optional.isPresent() && !optional.get().getId().equals(id)) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }

        if (isNumberLessThanOne(fields, MIN_DURATION)) {
            return new ResponseEntity<>(new ErrorResponse(HttpStatus.BAD_REQUEST, "Duration must be equal or greater than one!"
                    , "Number not valid!"), HttpStatus.BAD_REQUEST);
        }

        if (isNumberLessThanOne(fields, PRICE)) {
            return new ResponseEntity<>(new ErrorResponse(HttpStatus.BAD_REQUEST, "Price must be equal or greater than one!"
                    , "Number not valid!"), HttpStatus.BAD_REQUEST);
        }

        Activity activity = this.activityService.update(optionalActivity.get(), fields);
        this.activityService.register(activity);

        return super.successResponse();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Response> delete(@PathVariable Long id) {
        Optional<Activity> optionalActivity = this.activityService.findById(id);
        if (!optionalActivity.isPresent()) {
            return new ResponseEntity<>(new ErrorResponse(HttpStatus.NOT_FOUND, NO_SUCH_ACTIVITY, ACTIVITY_NOT_FOUND)
                    , HttpStatus.NOT_FOUND);
        }
        this.activityService.delete(optionalActivity.get());

        return new ResponseEntity<>(new SuccessResponse(HttpStatus.OK, "Activity is deleted!"), HttpStatus.OK);
    }

    private boolean isValidAppointment(Activity activity, Long appointmentId, BaseService<Appointment> baseService) {
        if (appointmentId != null) {
            Optional<Appointment> optionalAppointment = baseService.findById(appointmentId);
            if (!optionalAppointment.isPresent()) {
                return false;
            } else {
                activity.getAppointments().add(optionalAppointment.get());
            }
        }
        return true;
    }

    private boolean isNumberLessThanOne(Map<String, Object> fields, String value) {
        if (!fields.containsKey(value)) {
            return false;
        }
        boolean durationLessThanOne;
        try {
            durationLessThanOne = (Integer) fields.get(value) < 1;
        } catch (Exception ex) {
            durationLessThanOne = (Double) fields.get(value) < 1;
        }
        return durationLessThanOne;
    }
}
