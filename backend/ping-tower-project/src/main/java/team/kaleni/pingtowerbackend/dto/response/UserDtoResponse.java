package team.kaleni.pingtowerbackend.dto.response;

import lombok.Builder;
import lombok.Data;
import lombok.Value;

@Data
@Value
@Builder
public class UserDtoResponse {
    Long id;
    String username;
    String email;
}
