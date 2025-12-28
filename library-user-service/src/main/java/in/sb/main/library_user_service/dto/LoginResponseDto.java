package in.sb.main.library_user_service.dto;

import in.sb.main.library_user_service.enums.UserRole;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginResponseDto {
    private String token;
    private UserDto userDto;
}
