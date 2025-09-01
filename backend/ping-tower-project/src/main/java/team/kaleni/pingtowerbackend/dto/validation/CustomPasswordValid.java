package team.kaleni.pingtowerbackend.dto.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = CustomPasswordValidator.class)
public @interface CustomPasswordValid {
    String message() default "Пароль должен состоять из >8 символов, содержать хотя бы 1 цифру, 1 заглавную букву, 1 специальный символ и содержать только латинские буквы.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}