package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exceptions.ObjectNotFoundException;
import ru.practicum.shareit.item.dao.ItemDao;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.service.UserService;

import javax.validation.ValidationException;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemDao itemDao;
    private final UserService userService;

    @Override
    public ItemDto addItem(long userId, ItemDto itemDto) {
        validateItemDto(itemDto, false);
        Item item = ItemMapper.toItem(itemDto);
        UserDto user = userService.getUserById(userId);
        item.setOwner(UserMapper.toUser(user));
        return ItemMapper.toItemDto(itemDao.addItem(item));
    }

    @Override
    public ItemDto updateItem(long userId, ItemDto itemDto) {
        Item item = ItemMapper.toItem(itemDto);
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
        return ItemMapper.toItemDto(itemDao.updateItem(item));
    }

    @Override
    public ItemDto getItemById(long id) {
        return ItemMapper.toItemDto(itemDao.getItemById(id));
    }

    @Override
    public List<ItemDto> getAllItemsByUserId(long userId) {
        List<ItemDto> itemsDto = new ArrayList<>();
        for (Item item : itemDao.getAllItemsByUserId(userId)) {
            itemsDto.add(ItemMapper.toItemDto(item));
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
            itemsDto.add(ItemMapper.toItemDto(item));
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
}
