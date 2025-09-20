package team.kaleni.pingtowerbackend.userservice.exceptionhandling.handler;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.context.request.WebRequest;

@ControllerAdvice
public abstract class BaseControllerAdvice {

    protected String getUrl(WebRequest request) {
        return request.getDescription(false).replace("uri=", "");
    }
}