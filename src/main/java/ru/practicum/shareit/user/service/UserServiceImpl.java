package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exceptions.ConflictException;
import ru.practicum.shareit.exceptions.ObjectNotFoundException;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.model.User;

import javax.transaction.Transactional;
import javax.validation.ValidationException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    public UserDto addUser(UserDto userDto) {
        User user = UserMapper.INSTANCE.toUser(userDto);
        validateUser(user);
        return UserMapper.INSTANCE.toUserDto(userRepository.save(user));
    }

    @Override
    public UserDto updateUser(UserDto userDto) {
        User newUser = UserMapper.INSTANCE.toUser(userDto);
        var user = userRepository.findById(newUser.getId()).get();
        if (user == null) {
            throw new ValidationException("Такой пользователь не существует!");
        }
        if (newUser.getEmail() != null) {
            newUser.setEmail(user.getEmail());
        }
        if (newUser.getName() != null) {
            newUser.setName(user.getName());
        }
        validateUser(newUser);
        return UserMapper.INSTANCE.toUserDto(userRepository.save(user));
    }

    @Transactional
    @Override
    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(UserMapper.INSTANCE::toUserDto)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteUser(long id) {
        if (!userExists(id)) {
            throw new ObjectNotFoundException("Пользователя не существует!");
        }
        userRepository.deleteById(id);
    }

    @Override
    public UserDto getUserById(long id) {
        if (!userExists(id)) {
            throw new ObjectNotFoundException("Пользователя не существует!");
        }
        User user = userRepository.findById(id).get();
        validateUser(user);
        return UserMapper.INSTANCE.toUserDto(user);
    }

    private void validateUser(User user) {
        if (user.getEmail() == null || user.getEmail().isEmpty()) {
            throw new ValidationException("Email не может быть пустым!");
        }
        if (!user.getEmail().contains("@")) {
            throw new ValidationException("Email должно содержать символ @");
        }
        if (user.getName() == null || user.getName().isEmpty() || user.getName().isBlank()) {
            throw new ObjectNotFoundException("Пользователь не найден");
        }
    }

    private boolean userExists(long userId) {
        boolean isExist = false;
        for (User user : userRepository.findAll()) {
            if (Objects.equals(user.getId(), userId)) {
                isExist = true;
                break;
            }
        }
        return isExist;
    }

    private boolean emailExists(User user) {
        for (User user1 : userRepository.findAll()) {
            if (user1.getEmail().contains(user.getEmail())) {
                if (!Objects.equals(user1.getId(), user.getId())) {
                    log.info("Пользователь с такой почтой уже существует!");
                    return true;
                }
            }
        }
        return false;
    }
}