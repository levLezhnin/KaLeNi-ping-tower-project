package team.kaleni.pingtowerbackend.dto.exceptions;

import lombok.Builder;
import lombok.Value;

@Builder
@Value
public class ValidationError {
    String field;
    String message;
}
