package in.sb.main.library_user_service.controller;

import in.sb.main.library_user_service.dto.RegisterRequestDto;
import in.sb.main.library_user_service.dto.UserDto;
import in.sb.main.library_user_service.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<UserDto> registerMember(@Valid @RequestBody RegisterRequestDto registerRequestDto) {
        log.info("In - UserController.registerMember() - email: {}", registerRequestDto.getEmail());
        UserDto userDto = userService.registerMember(registerRequestDto);
        log.info("Out - UserController.registerMember() - userId: {}, email: {}", userDto.getId(), userDto.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED).body(userDto);
    }

    @PostMapping("/register/librarian")
    public ResponseEntity<UserDto> registerLibrarian(@Valid @RequestBody RegisterRequestDto registerRequestDto) {
        log.info("In - UserController.registerLibrarian() - email: {}", registerRequestDto.getEmail());
        UserDto userDto = userService.registerLibrarian(registerRequestDto);
        log.info("Out - UserController.registerLibrarian() - userId: {}, email: {}, role: {}", userDto.getId(), userDto.getEmail(), userDto.getRole());
        return ResponseEntity.status(HttpStatus.CREATED).body(userDto);
    }

    @GetMapping("/profile")
    public ResponseEntity<UserDto> getUserProfileInfo(Authentication authentication) {
        log.info("In - UserController.getUserProfileInfo()");
        if (authentication == null || !authentication.isAuthenticated()) {
            log.warn("Out - UserController.getUserProfileInfo() - Unauthorized access attempt");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String email = authentication.getName();
        log.info("In - UserController.getUserProfileInfo() - email: {}", email);
        UserDto userDto = userService.getUserProfileByEmail(email);
        log.info("Out - UserController.getUserProfileInfo() - userId: {}, email: {}", userDto.getId(), userDto.getEmail());
        return ResponseEntity.ok(userDto);
    }

}
