package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exceptions.ObjectNotFoundException;
import ru.practicum.shareit.item.dao.ItemDao;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.dao.UserDao;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.model.User;

import javax.validation.ValidationException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemDao itemDao;
    private final UserDao userDao;

    @Override
    public ItemDto addItem(long userId, ItemDto itemDto) {
        validateItemDto(itemDto, false);
        Item item = ItemMapper.INSTANCE.toItem(itemDto);
        if (!userExists(userId)) {
            throw new ObjectNotFoundException("Пользователя не существует!");
        }
        UserDto user = UserMapper.INSTANCE.toUserDto(userDao.getUserById(userId));
        item.setOwner(UserMapper.INSTANCE.toUser(user));
        return ItemMapper.INSTANCE.toItemDto(itemDao.addItem(item));
    }

    @Override
    public ItemDto updateItem(long userId, ItemDto itemDto) {
        Item item = ItemMapper.INSTANCE.toItem(itemDto);
        Item currentItem = itemDao.getItemById(itemDto.getId());
        if (currentItem == null) {
            throw new ObjectNotFoundException("Такой вещи не существует!");
        }
        if (userId != currentItem.getOwner().getId()) {
            throw new ObjectNotFoundException("Id пользователя не совпадает с id владельца.");
        }

        item.setOwner(currentItem.getOwner());
        if (itemDto.getName() == null) {
            item.setName(currentItem.getName());
        }
        if (itemDto.getDescription() == null) {
            item.setDescription(currentItem.getDescription());
        }
        if (itemDto.getAvailable() == null) {
            item.setAvailable(currentItem.isAvailable());
        }

        validateItemDto(itemDto, true);
        return ItemMapper.INSTANCE.toItemDto(itemDao.updateItem(item));
    }

    @Override
    public ItemDto getItemById(long id) {
        return ItemMapper.INSTANCE.toItemDto(itemDao.getItemById(id));
    }

    @Override
    public List<ItemDto> getAllItemsByUserId(long userId) {
        List<ItemDto> itemsDto = new ArrayList<>();
        for (Item item : itemDao.getAllItemsByUserId(userId)) {
            itemsDto.add(ItemMapper.INSTANCE.toItemDto(item));
        }
        return itemsDto;
    }

    @Override
    public List<ItemDto> textSearch(String text) {
        List<ItemDto> itemsDto = new ArrayList<>();
        if (text.isBlank()) {
            return itemsDto;
        }
        for (Item item : itemDao.textSearch(text)) {
            itemsDto.add(ItemMapper.INSTANCE.toItemDto(item));
        }
        return itemsDto;
    }

    private void validateItemDto(ItemDto itemDto, boolean isUpdate) {
        if (isUpdate && (itemDto.getName() != null && itemDto.getName().isBlank()) ||
                (!isUpdate && (itemDto.getName() == null || itemDto.getName().isBlank()))) {
            throw new ValidationException("Не указано поле Name");
        }

        if (isUpdate && (itemDto.getDescription() != null && itemDto.getDescription().isBlank()) ||
                !isUpdate && (itemDto.getDescription() == null || itemDto.getDescription().isBlank())) {
            throw new ValidationException("Не указано поле Description");
        }

        if (!isUpdate && itemDto.getAvailable() == null) {
            throw new ValidationException("Отсуствует поле Accessible");
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
}
