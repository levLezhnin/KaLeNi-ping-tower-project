package team.kaleni.pingtowerbackend.userservice.dto.request;

import lombok.Data;
import lombok.Value;
import team.kaleni.pingtowerbackend.userservice.dto.validation.CustomEmailValid;
import team.kaleni.pingtowerbackend.userservice.dto.validation.CustomPasswordValid;

@Data
@Value
public class UserCredentialsDto {
    @CustomEmailValid
    String email;
    @CustomPasswordValid
    String password;
}
