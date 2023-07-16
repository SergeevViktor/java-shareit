package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;

import javax.validation.Valid;
import java.net.URI;
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
    public ResponseEntity<List<ItemDto>> getAllItemsByUserId(@RequestHeader("X-Sharer-User-Id") long userId) {
        return ResponseEntity.ok().body(itemService.getAllItemsByUserId(userId));
    }

    @PostMapping
    public ResponseEntity<ItemDto> addItem(@Valid @RequestBody ItemDto itemDto,
                           @RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("Добавление вещи.");
        return ResponseEntity.created(URI.create("http://localhost:8080/items"))
                .body(itemService.addItem(userId, itemDto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ItemDto> getItemById(@PathVariable long id) {
        return ResponseEntity.ok().body(itemService.getItemById(id));
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<ItemDto> updateItem(@RequestHeader("X-Sharer-User-Id") long userId,
                              @PathVariable long itemId,
                              @RequestBody ItemDto itemDto) {

        itemDto.setId(itemId);
        log.info("Обновление вещи.");
        return ResponseEntity.ok().body(itemService.updateItem(userId, itemDto));
    }

    @GetMapping("/search")
    public ResponseEntity<List<ItemDto>> textSearch(@RequestParam(value = "text") String text) {
        return ResponseEntity.ok().body(itemService.textSearch(text));
    }
}