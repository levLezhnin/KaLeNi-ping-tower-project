package team.kaleni.pingtowerbackend.userservice.exceptionhandling.handler;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import team.kaleni.pingtowerbackend.userservice.dto.exceptions.ErrorResponse;
import team.kaleni.pingtowerbackend.userservice.dto.exceptions.ErrorResponseBuilder;

import static org.springframework.http.HttpStatus.CONFLICT;

@ControllerAdvice
@RequiredArgsConstructor
@Slf4j
public class ServiceExceptionHandler extends BaseControllerAdvice {

    private final ErrorResponseBuilder errorResponseBuilder;

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleEntityNotFound(EntityNotFoundException e, WebRequest request) {
        log.info("EntityNotFoundException: {}", e.getMessage());
        log.debug(e.getMessage(), e);

        return new ResponseEntity<>(errorResponseBuilder.mapWithoutValid(CONFLICT, e.getMessage(), getUrl(request)),
                HttpStatus.CONFLICT);
    }

    @ExceptionHandler(EntityExistsException.class)
    public ResponseEntity<?> handleEntityExistingException(EntityExistsException e, WebRequest request) {
        log.info("EntityExistsException: {}", e.getMessage());
        log.debug(e.getMessage(), e);

        return new ResponseEntity<>(errorResponseBuilder.mapWithoutValid(CONFLICT, e.getMessage(), getUrl(request)),
                CONFLICT);
    }

}
