package team.kaleni.pingtowerbackend.userservice.service;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import team.kaleni.pingtowerbackend.userservice.dto.mapper.UserMapper;
import team.kaleni.pingtowerbackend.userservice.dto.request.UserCredentialsDto;
import team.kaleni.pingtowerbackend.userservice.dto.request.UserInsertDtoRequest;
import team.kaleni.pingtowerbackend.userservice.dto.request.UserUpdateDtoRequest;
import team.kaleni.pingtowerbackend.userservice.dto.response.UserDtoResponse;
import team.kaleni.pingtowerbackend.userservice.entity.User;
import team.kaleni.pingtowerbackend.userservice.repository.UserRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public UserDtoResponse insert(UserInsertDtoRequest userInsertDtoRequest) {
        User user = userMapper.toDomain(userInsertDtoRequest);

        // проверяем, что пользователя с таким email не существует
        Optional<User> optEmail = userRepository.findByEmail(user.getEmail());
        if (optEmail.isPresent()) {
            throw new EntityExistsException("Пользователь с email-ом: " + user.getEmail() + " уже существует!");
        }

        // проверяем, что пользователя с таким username не существует
        Optional<User> optUsername = userRepository.findByUsername(user.getUsername());
        if (optUsername.isPresent()) {
            throw new EntityExistsException("Пользователь с именем: " + user.getUsername() + " уже существует!");
        }

        return userMapper.toDto(userRepository.saveAndFlush(user));
    }

    public UserDtoResponse update(Long id, UserUpdateDtoRequest userUpdateDtoRequest) {

        // проверяем, что пользователь с таким id существует
        Optional<User> optId = userRepository.findById(id);
        if (optId.isEmpty()) {
            throw new EntityNotFoundException("Пользователь с id: " + id + " не найден.");
        }

        //проверяем, что то имя, на которое мы хотим поменять старое, уникально
        Optional<User> optUsername = userRepository.findByUsername(userUpdateDtoRequest.getUsername());
        if (optUsername.isPresent() && !optUsername.get().getId().equals(id)) {
            throw new EntityExistsException("Пользователь с именем: " + userUpdateDtoRequest.getUsername() + " уже существует!");
        }

        User user = optId.get();
        user.setUsername(userUpdateDtoRequest.getUsername());
        user.setPasswordHash(passwordEncoder.encode(userUpdateDtoRequest.getPassword()));

        return userMapper.toDto(userRepository.saveAndFlush(user));
    }

    public void updateTelegramInfo(Long id, Long chatId) {

        Optional<User> optUser = userRepository.findById(id);

        if (optUser.isEmpty()) {
            throw new EntityNotFoundException("Пользователь с id: " + id + " не найден.");
        }

        User user = optUser.get();
        user.setTelegramChatId(chatId);

        userRepository.saveAndFlush(user);
    }

    public void unsubscribeUserByChatId(Long chatId) {

        Optional<User> optUser = userRepository.findByTelegramChatId(chatId);

        if (optUser.isPresent()) {
            User user = optUser.get();
            user.setTelegramChatId(null);
            userRepository.saveAndFlush(user);
        }
    }

    public boolean signIn(UserCredentialsDto userCredentialsDto) {

        // проверяем есть ли пользователь с таким email-ом
        Optional<User> optEmail = userRepository.findByEmail(userCredentialsDto.getEmail());
        if (optEmail.isEmpty()) {
            return false;
        }

        User user = optEmail.get();

        // проверяем совпадают ли пароли
        return passwordEncoder.matches(userCredentialsDto.getPassword(), user.getPasswordHash());
    }

    User findEntityById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь с id: " + id + " не найден."));
    }

    public UserDtoResponse findById(Long id) {
        return userMapper.toDto(
                findEntityById(id)
        );
    }

    public UserDtoResponse findByEmail(String email) {
        return userMapper.toDto(
                userRepository.findByEmail(email)
                        .orElseThrow(() -> new EntityNotFoundException("Пользователь с email: " + email + " не найден."))
        );
    }

    public List<UserDtoResponse> findAllUsersByUsernameStartsWith(String username) {
        return userRepository.findAllByUsernameStartsWithIgnoreCase(username)
                .stream()
                .map(userMapper::toDto)
                .toList();
    }
}
