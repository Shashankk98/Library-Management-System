package in.sb.main.library_user_service.dto;
import in.sb.main.library_user_service.enums.UserRole;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
@Data
@Builder
public class UserDto {
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private UserRole role;
    private Boolean active;
    private LocalDateTime createdAt;
}
