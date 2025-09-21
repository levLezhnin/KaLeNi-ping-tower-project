package team.kaleni.pingtowerbackend.userservice.dto.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import team.kaleni.pingtowerbackend.userservice.dto.request.UserInsertDtoRequest;
import team.kaleni.pingtowerbackend.userservice.dto.response.UserDtoResponse;
import team.kaleni.pingtowerbackend.userservice.entity.User;

@Component
@RequiredArgsConstructor
public class UserMapper implements BaseDtoDomainMapper<UserInsertDtoRequest, User, UserDtoResponse> {

    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDtoResponse toDto(User domain) {
        return UserDtoResponse.builder()
                .id(domain.getId())
                .email(domain.getEmail())
                .username(domain.getUsername())
                .telegramChatId(domain.getTelegramChatId())
                .build();
    }

    @Override
    public User toDomain(UserInsertDtoRequest requestDto) {
        return User.builder()
                .username(requestDto.getUsername())
                .email(requestDto.getEmail())
                .passwordHash(passwordEncoder.encode(requestDto.getPassword()))
                .build();
    }
}
