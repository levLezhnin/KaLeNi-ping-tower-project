package team.kaleni.pingtowerbackend.userservice.dto.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class ErrorResponseBuilder {

    public ErrorResponse mapWithoutValid(HttpStatus httpStatus, String message, String url) {
        return ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(httpStatus)
                .message(message)
                .url(url)
                .build();
    }

    public ErrorResponse mapWithValid(HttpStatus httpStatus, String url,
                                      List<ValidationError> validationErrors) {
        return ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(httpStatus)
                .validationErrors(validationErrors)
                .url(url)
                .build();
    }
}