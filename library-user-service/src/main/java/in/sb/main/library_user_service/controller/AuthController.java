package in.sb.main.library_user_service.controller;

import in.sb.main.library_user_service.dto.LoginRequestDto;
import in.sb.main.library_user_service.dto.LoginResponseDto;
import in.sb.main.library_user_service.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@Valid @RequestBody LoginRequestDto loginRequestDto) {
        log.info("In - AuthController.login() - email: {}", loginRequestDto.getEmail());
        LoginResponseDto loginResponse = authService.login(loginRequestDto);
        log.info("Out - AuthController.login() - email: {}, role: {}", loginRequestDto.getEmail(), loginResponse.getUserDto().getRole());
        return ResponseEntity.ok(loginResponse);
    }
}

