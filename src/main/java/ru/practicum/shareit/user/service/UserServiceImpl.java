package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exceptions.ConflictException;
import ru.practicum.shareit.exceptions.ObjectNotFoundException;
import ru.practicum.shareit.user.dao.UserDao;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.model.User;

import javax.validation.ValidationException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserDao userDao;

    @Override
    public UserDto addUser(UserDto userDto) {
        User user = UserMapper.INSTANCE.toUser(userDto);
        validateUser(user);
        if (emailExists(user)) {
            throw new ConflictException("Такого email уже существует.");
        }
        return UserMapper.INSTANCE.toUserDto(userDao.addUser(user));
    }

    @Override
    public UserDto updateUser(UserDto userDto) {
        User newUser = UserMapper.INSTANCE.toUser(userDto);
        User user = userDao.getUserById(newUser.getId());

        if (user == null) {
            throw new ValidationException("Такой пользователь не существует!");
        }
        if (newUser.getEmail() == null) {
            newUser.setEmail(user.getEmail());
        }
        if (newUser.getName() == null) {
            newUser.setName(user.getName());
        }

        validateUser(newUser);
        if (emailExists(newUser)) {
            throw new ConflictException("Такой email уже существует.");
        }
        return UserMapper.INSTANCE.toUserDto(userDao.updateUser(newUser));
    }

    @Override
    public List<UserDto> getAllUsers() {
        return userDao.getAllUsers().stream()
                .map(UserMapper.INSTANCE::toUserDto)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteUser(long id) {
        if (!userExists(id)) {
            throw new ObjectNotFoundException("Пользователя не существует!");
        }
        userDao.deleteUser(id);
    }

    @Override
    public UserDto getUserById(long id) {
        if (!userExists(id)) {
            throw new ObjectNotFoundException("Пользователя не существует!");
        }
        User user = userDao.getUserById(id);
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
        for (User user : userDao.getAllUsers()) {
            if (Objects.equals(user.getId(), userId)) {
                isExist = true;
                break;
            }
        }
        return isExist;
    }

    private boolean emailExists(User user) {
        for (User user1 : userDao.getAllUsers()) {
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