package team.kaleni.pingtowerbackend.dto.exceptions;

import lombok.Builder;
import lombok.Value;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.List;

@Builder
@Value
public class ErrorResponse {
    LocalDateTime timestamp;
    HttpStatus status;
    String message;
    String url;
    List<ValidationError> validationErrors;
}
