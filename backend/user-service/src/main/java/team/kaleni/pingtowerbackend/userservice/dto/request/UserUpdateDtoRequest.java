package team.kaleni.pingtowerbackend.userservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.Value;
import team.kaleni.pingtowerbackend.userservice.dto.validation.CustomPasswordValid;

@Data
@Value
public class UserUpdateDtoRequest {

    @NotBlank
    String username;

    @CustomPasswordValid
    String password;

}
