package team.kaleni.pingtowerbackend.userservice.exceptionhandling.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import team.kaleni.pingtowerbackend.userservice.dto.exceptions.ErrorResponse;
import team.kaleni.pingtowerbackend.userservice.dto.exceptions.ErrorResponseBuilder;
import team.kaleni.pingtowerbackend.userservice.dto.exceptions.ValidationError;

import java.util.List;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@ControllerAdvice
@RequiredArgsConstructor
@Slf4j
public class ControllerExceptionHandler extends BaseControllerAdvice {
    private final ErrorResponseBuilder errorResponseBuilder;

    //Обработка ошибок валидации
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleDtoValidatingException(MethodArgumentNotValidException e,
                                                                      WebRequest request) {
        log.info("MethodArgumentNotValidException: {}", e.getMessage());
        log.debug(e.getMessage(), e);

        List<ValidationError> validationErrors = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fields -> ValidationError
                        .builder()
                        .field(fields.getField())
                        .message(fields.getDefaultMessage())
                        .build())
                .toList();

        return new ResponseEntity<>(errorResponseBuilder.mapWithValid(BAD_REQUEST, getUrl(request),validationErrors),
                BAD_REQUEST);
    }
}
