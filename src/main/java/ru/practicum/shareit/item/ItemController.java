package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;

import javax.validation.Valid;
import java.util.List;

/**
 * TODO Sprint add-controllers.
 */
@RestController
@RequestMapping("/items")
@Slf4j
@RequiredArgsConstructor
public class ItemController {
    private final ItemService itemService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<ItemDto> getAllItemsByUserId(@RequestHeader("X-Sharer-User-Id") long userId) {
        return itemService.getAllItemsByUserId(userId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ItemDto addItem(@Valid @RequestBody ItemDto itemDto,
                           @RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("Добавление вещи.");
        return itemService.addItem(userId, itemDto);
    }

    @GetMapping("/{id}")
    public ItemDto getItemById(@PathVariable long id) {
        return itemService.getItemById(id);
    }

    @PatchMapping("/{itemId}")
    @ResponseStatus(HttpStatus.OK)
    public ItemDto updateItem(@RequestHeader("X-Sharer-User-Id") long userId,
                              @PathVariable long itemId,
                              @RequestBody ItemDto itemDto) {

        itemDto.setId(itemId);
        log.info("Обновление вещи.");
        return itemService.updateItem(userId, itemDto);
    }

    @GetMapping("/search")
    public List<ItemDto> textSearch(@RequestParam(value = "text") String text) {
        return itemService.textSearch(text);
    }
}
