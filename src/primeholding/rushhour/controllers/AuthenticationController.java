package primeholding.rushhour.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
import primeholding.rushhour.models.ApiResponse;
import primeholding.rushhour.models.JwtAuthenticationResponse;
import primeholding.rushhour.models.ModelMapper;
import primeholding.rushhour.models.SignUpModel;
import primeholding.rushhour.services.RoleService;
import primeholding.rushhour.services.UserService;

import javax.validation.Valid;

import primeholding.rushhour.models.LogInModel;
import primeholding.rushhour.security.JwtTokenProvider;

import java.util.Collections;
import java.util.Optional;

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
    public AuthenticationController(AuthenticationManager authenticationManager, UserService userService, ModelMapper mapper, PasswordEncoder passwordEncoder, JwtTokenProvider tokenProvider, RoleService roleService) {
        this.authenticationManager = authenticationManager;
        this.userService = userService;
        this.mapper = mapper;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
        this.roleService = roleService;
    }

    @PostMapping("/signup")
    public ResponseEntity<HttpStatus> registerUser(@RequestBody SignUpModel signUpModel){
        if(this.userService.existWithEmail(signUpModel.getEmail())) {
            return new ResponseEntity(new ApiResponse(false, "Email Address already in use!"),
                    HttpStatus.BAD_REQUEST);
        }

        Optional<Role> userRole = this.roleService.findByName(RoleName.USER);
        if(!userRole.isPresent()){ return new ResponseEntity(new ApiResponse(false, "User name not exists!"),
                HttpStatus.BAD_REQUEST);
        }
        User user = this.mapper.signUpToUser(signUpModel);
        user.setPassword(this.passwordEncoder.encode(user.getPassword()));
        user.setRoles(Collections.singleton(userRole.get()));

        this.userService.register(user);

        return new ResponseEntity(new ApiResponse(true, "User registered successfully"), HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<JwtAuthenticationResponse> authenticateUser(@Valid @RequestBody LogInModel logInModel) {

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
}
