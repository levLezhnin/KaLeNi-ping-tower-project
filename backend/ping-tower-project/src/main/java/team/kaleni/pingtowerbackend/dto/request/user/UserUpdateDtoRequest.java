package team.kaleni.pingtowerbackend.dto.request.user;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.Value;
import team.kaleni.pingtowerbackend.dto.validation.CustomPasswordValid;

@Data
@Value
public class UserUpdateDtoRequest {

    @NotBlank
    String username;

    @CustomPasswordValid
    String password;

}
