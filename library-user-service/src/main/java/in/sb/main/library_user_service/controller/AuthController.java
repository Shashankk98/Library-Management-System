package in.sb.main.library_user_service.controller;

import in.sb.main.library_user_service.dto.LoginRequestDto;
import in.sb.main.library_user_service.dto.LoginResponseDto;
import in.sb.main.library_user_service.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@Valid @RequestBody LoginRequestDto loginRequestDto) {
        LoginResponseDto loginResponse = authService.login(loginRequestDto);
        return ResponseEntity.ok(loginResponse);
    }
}

