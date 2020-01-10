package primeholding.rushhour.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import primeholding.rushhour.entities.Appointment;
import primeholding.rushhour.entities.User;
import primeholding.rushhour.models.ModelMapper;
import primeholding.rushhour.models.appointments.GetAppointmentModel;
import primeholding.rushhour.models.users.GetUserModel;
import primeholding.rushhour.models.users.PutUserModel;
import primeholding.rushhour.responses.Response;
import primeholding.rushhour.services.AppointmentService;
import primeholding.rushhour.services.UserService;

import javax.transaction.Transactional;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/users")
public class UserController extends BaseController {

    private UserService userService;

    private AppointmentService appointmentService;

    private ModelMapper mapper;

    private PasswordEncoder passwordEncoder;

    @Autowired
    public UserController(UserService userService, AppointmentService appointmentService, ModelMapper mapper, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.appointmentService = appointmentService;
        this.mapper = mapper;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<List<GetUserModel>> get() {
        List<User> allUsers = this.userService.findAll();
        List<GetUserModel> getUserModels = new ArrayList<>();
        for (User user : allUsers) {
            GetUserModel getUserModel = this.mapper.userToGetModel(user);
            getUserModel.setRole(user.getRoles());
            getUserModels.add(getUserModel);
        }

        return new ResponseEntity<>(getUserModels, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<GetUserModel> get(@PathVariable Long id) {
        Optional<User> optionalUser = this.userService.findById(id);
        if (!optionalUser.isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        GetUserModel getUserModel = this.mapper.userToGetModel(optionalUser.get());
        getUserModel.setRole(optionalUser.get().getRoles());

        return new ResponseEntity<>(getUserModel, HttpStatus.OK);
    }

    @GetMapping("{id}/appointments")
    public ResponseEntity<Set<GetAppointmentModel>> getAppointments(@PathVariable Long id) {
        if (!isUserExisting(id)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        List<Appointment> appointmentsByUserId = this.appointmentService.getAppointmentsByUserId(id);
        Set<GetAppointmentModel> getAppointmentModels = appointmentsByUserId
                .stream()
                .map(x -> this.mapper.appointmentToGetModel(x))
                .collect(Collectors.toSet());

        return new ResponseEntity<>(getAppointmentModels, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Response> put(@PathVariable Long id, @Valid @RequestBody PutUserModel putUserModel) {
        if (!isUserExisting(id)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        Optional<User> optional = this.userService.findByEmail(putUserModel.getEmail());
        if (optional.isPresent() && !optional.get().getId().equals(id)) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }

        User user = this.mapper.putModelToUser(putUserModel);
        user.setId(id);
        user.setPassword(this.passwordEncoder.encode(user.getPassword()));
        this.userService.register(user);

        return super.successResponse("Updated");
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Response> patch(@PathVariable Long id, @RequestBody Map<String, Object> fields) {
        if (!isUserExisting(id)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        if (fields.containsKey("email")) {
            Optional<User> optional = this.userService.findByEmail(fields.get("email").toString());
            if (optional.isPresent() && !optional.get().getId().equals(id)) {
                return new ResponseEntity<>(HttpStatus.CONFLICT);
            }
        }

        User user = this.userService.getUser(id);
        User update = this.userService.update(user, fields);

        if (fields.containsKey("password")) {
            update.setPassword(this.passwordEncoder.encode(update.getPassword()));
        }
        try {
            this.userService.register(update);
        } catch (TransactionSystemException e) {
           super.constraintViolationCheck(e);
        }

        return super.successResponse("Updated");
    }

    @Transactional
    @DeleteMapping("/{id}")
    public ResponseEntity<Response> delete(@PathVariable Long id) {
        if (!isUserExisting(id)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        this.userService.deleteById(id);

        return super.successResponse("Deleted");
    }

    private boolean isUserExisting(Long id) {
        return this.userService.findById(id).isPresent();
    }
}
