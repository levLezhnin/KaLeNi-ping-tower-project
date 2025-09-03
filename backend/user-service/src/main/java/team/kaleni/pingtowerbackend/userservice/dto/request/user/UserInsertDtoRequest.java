package team.kaleni.pingtowerbackend.userservice.dto.request.user;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;
import lombok.Value;
import team.kaleni.pingtowerbackend.userservice.dto.validation.CustomEmailValid;
import team.kaleni.pingtowerbackend.userservice.dto.validation.CustomPasswordValid;

@Data
@Value
@Builder
public class UserInsertDtoRequest {
    @NotBlank
    String username;

    @CustomEmailValid
    String email;

    @CustomPasswordValid
    String password;
}
