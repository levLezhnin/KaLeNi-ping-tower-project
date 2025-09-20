package team.kaleni.pingtowerbackend.userservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import team.kaleni.pingtowerbackend.userservice.dto.request.UserUpdateDtoRequest;
import team.kaleni.pingtowerbackend.userservice.dto.response.UserDtoResponse;
import team.kaleni.pingtowerbackend.userservice.service.UserService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(
            summary = "Нужен для обновления логина и/или пароля пользователя.",
            description = """
                    Принимает в себя json с логином и паролем.
                    
                    Логин должен быть уникален.
                    
                    Пароль должен состоять из >8 символов, должен содержать хотя бы 1 цифру, 1 заглавную букву, 1 специальный символ и содержать только латинские буквы.
                    
                    Возвращает json с id, логином и email-ом обновлённого пользователя.
                    """
    )
    @PutMapping("/update/{userId}")
    public UserDtoResponse update(@PathVariable Long userId,
                                  @RequestBody @Valid UserUpdateDtoRequest userUpdateDtoRequest) {
        return userService.update(userId, userUpdateDtoRequest);
    }

    @Operation(
            summary = """
                    Ищет пользователя по id.
                    """
    )
    @GetMapping("/{userId}")
    public UserDtoResponse findUserById(@PathVariable Long userId) {
        return userService.findById(userId);
    }

    @Operation(
            summary = """
                    Отбирает пользователей, у которых логин начинается на {prefix} без учёта регистра.
                    """
    )
    @GetMapping("/usernameStartsWith/{prefix}")
    public List<UserDtoResponse> findAllUsersByUsernameStartsWith(@PathVariable String prefix) {
        return userService.findAllUsersByUsernameStartsWith(prefix);
    }
    @Operation(
            summary = """
                    Ищет пользователя по email-у.
                    """
    )
    @GetMapping("/email/{email}")
    public UserDtoResponse findUserByEmail(@PathVariable String email) {
        return userService.findByEmail(email);
    }
}
