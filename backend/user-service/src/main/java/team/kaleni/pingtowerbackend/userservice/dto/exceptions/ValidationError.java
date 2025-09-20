package team.kaleni.pingtowerbackend.userservice.dto.exceptions;

import lombok.Builder;
import lombok.Value;

@Builder
@Value
public class ValidationError {
    String field;
    String message;
}
