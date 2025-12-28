package in.sb.main.library_user_service.controller;

import in.sb.main.library_user_service.dto.RegisterRequestDto;
import in.sb.main.library_user_service.dto.UserDto;
import in.sb.main.library_user_service.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<UserDto> registerMember(@Valid @RequestBody RegisterRequestDto registerRequestDto) {
        UserDto userDto = userService.registerMember(registerRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(userDto);
    }

    @PostMapping("/register/librarian")
    public ResponseEntity<UserDto> registerLibrarian(@Valid @RequestBody RegisterRequestDto registerRequestDto) {
        UserDto userDto = userService.registerLibrarian(registerRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(userDto);
    }

    @GetMapping("/profile")
    public ResponseEntity<UserDto> getUserProfileInfo(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String email = authentication.getName();
        UserDto userDto = userService.getUserProfileByEmail(email);
        return ResponseEntity.ok(userDto);
    }

}
