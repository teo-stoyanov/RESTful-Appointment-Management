package primeholding.rushhour.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import primeholding.rushhour.entities.Role;
import primeholding.rushhour.entities.RoleName;
import primeholding.rushhour.entities.User;
import primeholding.rushhour.models.LogInModel;
import primeholding.rushhour.models.ModelMapper;
import primeholding.rushhour.models.RegisterModel;
import primeholding.rushhour.responses.ErrorResponse;
import primeholding.rushhour.responses.JwtAuthenticationResponse;
import primeholding.rushhour.responses.Response;
import primeholding.rushhour.responses.SuccessResponse;
import primeholding.rushhour.security.JwtTokenProvider;
import primeholding.rushhour.services.RoleService;
import primeholding.rushhour.services.UserService;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@SuppressWarnings("All")
@RestController
@RequestMapping("/auth")
public class AuthenticationController {
    private AuthenticationManager authenticationManager;

    private UserService userService;

    private ModelMapper mapper;

    private PasswordEncoder passwordEncoder;

    private JwtTokenProvider tokenProvider;

    private RoleService roleService;

    @Autowired
    public AuthenticationController(AuthenticationManager authenticationManager, UserService userService,
                                    ModelMapper mapper, PasswordEncoder passwordEncoder, JwtTokenProvider tokenProvider,
                                    RoleService roleService) {
        this.authenticationManager = authenticationManager;
        this.userService = userService;
        this.mapper = mapper;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
        this.roleService = roleService;
    }

    @PostMapping("/register")
    public ResponseEntity<Response> registerUser(@RequestBody @Valid RegisterModel registerModel, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return returnErrorResponse(bindingResult);
        } else if (this.userService.existWithEmail(registerModel.getEmail())) {
            return new ResponseEntity<>(new ErrorResponse(400, "Emain exists", "Email Address already in use!")
                    , HttpStatus.BAD_REQUEST);
        }

        Optional<Role> userRole = this.roleService.findByName(RoleName.USER);
        if (!userRole.isPresent()) {
            return new ResponseEntity(new ErrorResponse(400, "Can't find User name", "User name not exists!")
                    , HttpStatus.BAD_REQUEST);
        }
        User user = this.mapper.signUpToUser(registerModel);
        user.setPassword(this.passwordEncoder.encode(user.getPassword()));
        user.setRoles(Collections.singleton(userRole.get()));

        this.userService.register(user);

        return returnSuccessResponse();
    }

    @PostMapping("/login")
    public ResponseEntity<Response> authenticateUser(@Valid @RequestBody LogInModel logInModel, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return returnErrorResponse(bindingResult);
        }

        Authentication authentication = this.authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        logInModel.getEmail(),
                        logInModel.getPassword()
                )
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String jwt = this.tokenProvider.generateToken(authentication);
        return ResponseEntity.ok(new JwtAuthenticationResponse(jwt));
    }

    private ResponseEntity<Response> returnErrorResponse(BindingResult bindingResult) {
        ErrorResponse error = new ErrorResponse();
        List<FieldError> errors = bindingResult.getFieldErrors();
        List<String> message = new ArrayList<>();
        error.setCode(400);
        for (FieldError e : errors) {
            message.add(e.getDefaultMessage());
        }
        error.setMessage("Update Failed");
        error.setCause(message.toString());
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    private ResponseEntity<Response> returnSuccessResponse() {
        SuccessResponse successResponse = new SuccessResponse();
        successResponse.setCode(200);
        successResponse.setMessage("Successfully created!");
        return new ResponseEntity<>(successResponse, HttpStatus.OK);
    }
}
