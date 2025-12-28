package in.sb.main.library_user_service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequestDto
{
    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @Email(message = "Email should be valid")
    @NotBlank(message = "Email name is required")
    private String email;

    @NotBlank(message = "Password name is required")
    @Size(min = 6, message = "Password must be at least 6 characters long")
    private String password;

}
