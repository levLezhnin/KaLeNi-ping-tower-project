package team.kaleni.pingtowerbackend.dto.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.Getter;
import org.springframework.stereotype.Component;

@Component
@Getter
public class CustomPasswordValidator implements ConstraintValidator<CustomPasswordValid, String> {
    private final String PASSWORD_REGEX =
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{}|;':\",./<>?]).{8,256}$";

    @Override
    public boolean isValid(String s, ConstraintValidatorContext constraintValidatorContext) {
        return s != null && !s.isEmpty() && s.matches(PASSWORD_REGEX);
    }
}