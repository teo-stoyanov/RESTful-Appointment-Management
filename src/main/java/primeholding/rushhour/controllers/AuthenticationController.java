package primeholding.rushhour.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import primeholding.rushhour.entities.Role;
import primeholding.rushhour.entities.RoleName;
import primeholding.rushhour.entities.User;
import primeholding.rushhour.models.LogInModel;
import primeholding.rushhour.models.ModelMapper;
import primeholding.rushhour.models.users.GetUserModel;
import primeholding.rushhour.models.users.PostUserModel;
import primeholding.rushhour.responses.ErrorResponse;
import primeholding.rushhour.responses.JwtAuthenticationResponse;
import primeholding.rushhour.responses.Response;
import primeholding.rushhour.security.JwtTokenProvider;
import primeholding.rushhour.services.RoleService;
import primeholding.rushhour.services.UserService;

import javax.validation.Valid;
import java.util.Collections;

@RestController
@RequestMapping("/auth")
public class AuthenticationController extends BaseController {

    private AuthenticationManager authenticationManager;

    private UserService userService;

    private ModelMapper mapper;

    private PasswordEncoder passwordEncoder;

    private JwtTokenProvider tokenProvider;

    private RoleService roleService;

    @Value("${app.token.type}")
    private String tokenType;


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
    public ResponseEntity<?> registerEntity(@RequestBody @Valid PostUserModel postUserModel) {
        return registerEntity(postUserModel, this.userService, this.roleService, this.mapper,
                this.passwordEncoder, RoleName.USER);
    }

    @PostMapping("/register/admin")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<?> registerUser(@RequestBody @Valid PostUserModel postUserModel) {
        return registerEntity(postUserModel, this.userService, this.roleService, this.mapper, this.passwordEncoder, RoleName.ADMIN);
    }

    @PostMapping("/login")
    public ResponseEntity<Response> authenticateUser(@Valid @RequestBody LogInModel logInModel) {
        if (!this.userService.existWithEmail(logInModel.getEmail())) {
            return new ResponseEntity<>(new ErrorResponse(HttpStatus.NOT_FOUND, "Email not found", "Email not valid!")
                    , HttpStatus.BAD_REQUEST);
        }

        Authentication authentication = this.authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        logInModel.getEmail(),
                        logInModel.getPassword()
                )
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String jwt = this.tokenProvider.generateToken(authentication);

        return ResponseEntity.ok(new JwtAuthenticationResponse(jwt,this.tokenType));
    }

    private ResponseEntity<?> registerEntity(PostUserModel postUserModel,
                                                    UserService userService, RoleService roleService,
                                                    ModelMapper mapper, PasswordEncoder passwordEncoder, RoleName roleName) {
        if (userService.existWithEmail(postUserModel.getEmail())) {
            return new ResponseEntity<>(new ErrorResponse(HttpStatus.BAD_REQUEST, "Email exists", "Email Address already in use!")
                    , HttpStatus.BAD_REQUEST);
        }

        Role userRole = roleService.getByName(roleName);

        User user = mapper.registerToUser(postUserModel);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRoles(Collections.singleton(userRole));

        User register = userService.register(user);
        GetUserModel model = this.mapper.userToGetModel(register);
        model.setRole(register.getRoles());

        return new ResponseEntity<>(model,HttpStatus.CREATED);
    }
}
