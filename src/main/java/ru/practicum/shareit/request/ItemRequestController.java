package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.net.URI;
import java.util.List;

/**
 * TODO Sprint add-item-requests.
 */
@RestController
@RequestMapping(path = "/requests")
@Slf4j
@RequiredArgsConstructor
public class ItemRequestController {
    private final ItemRequestService itemRequestService;

    @GetMapping
    public ResponseEntity<List<ItemRequestResponseDto>> getItemsByUserId(@RequestHeader("X-Sharer-User-Id") long userId) {
        return ResponseEntity.ok().body(itemRequestService.getItemsRequests(userId));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<ItemRequestDto> addRequest(@RequestHeader("X-Sharer-User-Id") long userId,
                                                     @Valid @RequestBody ItemRequestDto itemRequestDto) {
        return ResponseEntity.created(URI.create("http://localhost:8080/requests"))
                .body(itemRequestService.addItemRequest(userId, itemRequestDto));
    }

    @GetMapping("/all")
    public ResponseEntity<List<ItemRequestResponseDto>> returnAll(@RequestHeader("X-Sharer-User-Id") long userId,
                                                  @RequestParam(defaultValue = "0") @Min(0) Integer from,
                                                  @RequestParam(defaultValue = "20") @Min(1) @Max(100) Integer size) {
        return ResponseEntity.ok().body(itemRequestService.getAllRequests(userId, from, size));
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<ItemRequestResponseDto> get(@RequestHeader("X-Sharer-User-Id") long userId,
                                                      @PathVariable long requestId) {
        return ResponseEntity.ok().body(itemRequestService.getRequestById(userId, requestId));
    }
}
