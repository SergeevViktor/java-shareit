package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ObjectNotFoundException;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.model.User;

import javax.transaction.Transactional;
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
        log.info("Добавлен новый пользователь; {}", user.getName());
        return UserMapper.INSTANCE.toUserDto(userRepository.save(user));
    }

    @Override
    public UserDto updateUser(UserDto userDto) {
        User newUser = UserMapper.INSTANCE.toUser(userDto);
        var user = userRepository.findById(newUser.getId()).get();
        if (newUser.getEmail() != null) {
            user.setEmail(newUser.getEmail());
        }
        if (newUser.getName() != null) {
            user.setName(newUser.getName());
        }
        log.info("Данные пользователя обновлены: {}", user.getName());
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
        return UserMapper.INSTANCE.toUserDto(user);
    }

    private boolean userExists(long userId) {
        var userOptional = userRepository.findById(userId);
        return !userOptional.isEmpty();
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