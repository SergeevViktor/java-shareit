package ru.practicum.shareit.user.dao;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.user.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
@Slf4j
public class InMemoryUserDao implements UserDao {
    private final Map<Long, User> users = new HashMap<>();
    private Long userId = 1L;

    @Override
    public User addUser(User user) {
        user.setId(nextId());
        users.put(user.getId(), user);
        log.info("Добавлен новый пользователь: {}", user);
        return user;
    }

    @Override
    public User updateUser(User user) {
        users.put(user.getId(), user);
        log.info("Данные пользователя обновлены: {}", user);
        return user;
    }

    @Override
    public List<User> getAllUsers() {
        return new ArrayList<>(users.values());
    }

    @Override
    public void deleteAllUsers() {
        log.info("Список пользователей очищен.");
        users.clear();
    }

    @Override
    public void deleteUser(long id) {
        log.info("Пользователь удален.");
        users.remove(id);
    }

    @Override
    public User getUserById(long id) {
        return users.get(id);
    }

    private Long nextId() {
        return userId++;
    }
}