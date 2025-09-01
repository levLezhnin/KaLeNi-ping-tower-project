package team.kaleni.pingtowerbackend.dto.request.user;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;
import lombok.Value;
import team.kaleni.pingtowerbackend.dto.validation.CustomEmailValid;
import team.kaleni.pingtowerbackend.dto.validation.CustomPasswordValid;

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
