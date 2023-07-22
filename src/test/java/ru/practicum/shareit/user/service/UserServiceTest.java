package ru.practicum.shareit.user.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.exceptions.ObjectNotFoundException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock
    UserRepository mockUserRepository;
    @Autowired
    UserService userService;

    @BeforeEach
    @Test
    void initializingService() {
        userService = new UserServiceImpl(mockUserRepository);
    }

    @Test
    void testAddUser() {

        Mockito
                .when(mockUserRepository.save(Mockito.any()))
                .thenReturn(new User(1L, "NameTest", "test@mail.ru"));
        UserDto userDto = UserDto.builder()
                .name("NameTest")
                .email("test@mail.ru").build();

        UserDto checkUserDto = userService.addUser(userDto);
        Assertions.assertEquals(1L, checkUserDto.getId(), "Поля объектов не совпадают");
        Assertions.assertEquals(checkUserDto.getName(), userDto.getName(), "Поля объектов не совпадают");
        Assertions.assertEquals(checkUserDto.getEmail(), userDto.getEmail(), "Поля объектов не совпадают");
    }

    @Test
    void testCreateUserWithEmptyBodyRequest() {

        final NullPointerException exception = Assertions.assertThrows(
                NullPointerException.class,
                () -> userService.addUser(null));

        Assertions.assertEquals("Cannot invoke \"ru.practicum.shareit.user.model.User.getEmail()\" " +
                        "because \"user\" is null", exception.getMessage());
    }

    @Test
    void testAddUserWithEmptyEmail() {
        UserDto userDto = UserDto.builder()
                .name("NameTest").build();

        final ValidationException exception = Assertions.assertThrows(
                ValidationException.class,
                () -> userService.addUser(userDto));

        Assertions.assertEquals("Email не может быть пустым!", exception.getMessage());
    }

    @Test
    void testAddUserWithIncorrectEmail() {
        UserDto userDto = UserDto.builder()
                .name("NameTest")
                .email("test.mail.ru").build();

        final ValidationException exception = Assertions.assertThrows(
                ValidationException.class,
                () -> userService.addUser(userDto));

        Assertions.assertEquals("Email должно содержать символ @", exception.getMessage());
    }

    @Test
    void testUpdateUser() {
        Mockito
                .when(mockUserRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(new User(1L, "NameTest", "test@mail.ru")));
        Mockito
                .when(mockUserRepository.save(Mockito.any()))
                .thenReturn(new User(1L, "UpdateTest", "UpdateTest@mail.ru"));

        UserDto userDto = UserDto.builder()
                .name("UpdateTest")
                .email("UpdateTest@mail.ru").build();

        UserDto checkUserDto = userService.updateUser(userDto);

        Assertions.assertEquals(checkUserDto.getName(), userDto.getName(), "Поля объектов не совпадают");
        Assertions.assertEquals(checkUserDto.getEmail(), userDto.getEmail(), "Поля объектов не совпадают");
    }

    @Test
    void testUpdateUserWithoutFieldName() {
        Mockito
                .when(mockUserRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(new User(1L, "NameTest", "test@mail.ru")));
        Mockito
                .when(mockUserRepository.save(Mockito.any()))
                .thenReturn(new User(1L, "NameTest", "UpdateTest@mail.ru"));

        UserDto userDto = UserDto.builder()
                .email("UpdateTest@mail.ru").build();

        UserDto checkUserDto = userService.updateUser(userDto);

        Assertions.assertEquals(checkUserDto.getName(), "NameTest", "Поля объектов не совпадают");
        Assertions.assertEquals(checkUserDto.getEmail(), userDto.getEmail(), "Поля объектов не совпадают");
    }

    @Test
    void testUpdateUserWithoutFieldEmail() {
        Mockito
                .when(mockUserRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(new User(1L, "NameTest", "test@mail.ru")));
        Mockito
                .when(mockUserRepository.save(Mockito.any()))
                .thenReturn(new User(1L, "UpdateTest", "test@mail.ru"));

        UserDto userDto = UserDto.builder()
                .name("UpdateTest")
                .email(null).build();

        UserDto checkUserDto = userService.updateUser(userDto);

        Assertions.assertEquals(checkUserDto.getName(), userDto.getName(), "Поля объектов не совпадают");
        Assertions.assertEquals(checkUserDto.getEmail(), "test@mail.ru", "Поля объектов не совпадают");
    }

    @Test
    void testGetUserById() {
        Mockito
                .when(mockUserRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(new User(1L, "NameTest", "test@mail.ru")));

        UserDto checkUserDto = userService.getUserById(1L);

        Assertions.assertEquals(1L, checkUserDto.getId(), "Поля объектов не совпадают");
        Assertions.assertEquals(checkUserDto.getName(), "NameTest", "Поля объектов не совпадают");
        Assertions.assertEquals(checkUserDto.getEmail(), "test@mail.ru", "Поля объектов не совпадают");
    }

    @Test
    void testGetUserByIdWithIncorrectId() {
        Mockito
                .when(mockUserRepository.findById(Mockito.anyLong()))
                .thenThrow(new ObjectNotFoundException("Пользователя не существует!"));

        final ObjectNotFoundException exception = Assertions.assertThrows(
                ObjectNotFoundException.class,
                () -> userService.getUserById(9999));

        Assertions.assertEquals("Пользователя не существует!", exception.getMessage());
    }

    @Test
    void testGetAllUsers() {
        List<User> users = Stream.of(new User(1L, "NameTest", "test@mail.ru"),
                new User(2L, "Name", "testMail@mail.ru")).collect(Collectors.toList());

        Mockito
                .when(mockUserRepository.findAll())
                .thenReturn(users);
        List<UserDto> checkUsers = userService.getAllUsers();

        Assertions.assertEquals(2, checkUsers.size(), "Размеры списков не совпадают");
        Assertions.assertEquals(checkUsers.get(0).getName(), "NameTest", "Поля объектов не совпадают");
        Assertions.assertEquals(checkUsers.get(1).getEmail(), "testMail@mail.ru", "Поля объектов не совпадают");
    }
}