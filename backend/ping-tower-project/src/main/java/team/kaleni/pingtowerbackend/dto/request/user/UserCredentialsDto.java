package team.kaleni.pingtowerbackend.dto.request.user;

import lombok.Data;
import lombok.Value;
import team.kaleni.pingtowerbackend.dto.validation.CustomEmailValid;
import team.kaleni.pingtowerbackend.dto.validation.CustomPasswordValid;

@Data
@Value
public class UserCredentialsDto {
    @CustomEmailValid String email;
    @CustomPasswordValid String password;
}
