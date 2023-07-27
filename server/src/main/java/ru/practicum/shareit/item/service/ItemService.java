package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemItemRequestDto;

import java.util.List;

public interface ItemService {

    ItemDto addItem(long userId, ItemItemRequestDto itemDto);

    ItemDto updateItem(long userId, ItemItemRequestDto itemDto);

    ItemDto getItemById(long itemId, long userId);

    List<ItemDto> getAllItemsByUserId(long userId);

    List<ItemDto> textSearch(String text);

    CommentDto addComment(long userId, long itemId, CommentDto commentDto);

}