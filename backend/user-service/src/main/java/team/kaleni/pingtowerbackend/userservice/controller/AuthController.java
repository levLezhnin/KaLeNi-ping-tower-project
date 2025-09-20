package team.kaleni.pingtowerbackend.userservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import team.kaleni.pingtowerbackend.userservice.dto.request.UserCredentialsDto;
import team.kaleni.pingtowerbackend.userservice.dto.request.UserInsertDtoRequest;
import team.kaleni.pingtowerbackend.userservice.dto.response.UserDtoResponse;
import team.kaleni.pingtowerbackend.userservice.service.UserService;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @Operation(
            summary = "Нужен для регистрации пользователя.",
            description = """
                    Принимает в себя json с логином, паролем и email-ом.
                    
                    Логин и email должны быть уникальны.
                    
                    Пароль должен состоять из >8 символов, должен содержать хотя бы 1 цифру, 1 заглавную букву, 1 специальный символ и содержать только латинские буквы.
                    
                    Возвращает json с id, логином и email-ом созданного пользователя.
                    """
    )
    @PostMapping("/register")
    public UserDtoResponse register(@RequestBody @Valid UserInsertDtoRequest userInsertDtoRequest) {
        return userService.insert(userInsertDtoRequest);
    }

    @Operation(
            summary = "Нужен для входа пользователя.",
            description = """
                    Принимает в себя json с email-ом и паролем.
                    
                    Пароль должен состоять из >8 символов, должен содержать хотя бы 1 цифру, 1 заглавную букву, 1 специальный символ и содержать только латинские буквы.
                    
                    Возвращает false, если email или пароль неверен. Возвращает true иначе.
                    """
    )
    @PostMapping("/signIn")
    public boolean signIn(@RequestBody @Valid UserCredentialsDto userCredentialsDto) {
        return userService.signIn(userCredentialsDto);
    }
}
