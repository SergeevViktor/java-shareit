package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.service.BookingService;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping(path = "/bookings")
@Slf4j
@RequiredArgsConstructor
public class BookingController {
    private final BookingService bookingService;

    @GetMapping
    public ResponseEntity<List<BookingDto>> getBookingsOfUser(@RequestHeader("X-Sharer-User-Id") long userId,
                                                              @RequestParam(defaultValue = "ALL") String state,
                                                              @RequestParam(defaultValue = "0") @Min(0) Integer from,
                                                              @RequestParam(defaultValue = "20") @Min(1) @Max(100) Integer size) {
        return ResponseEntity.ok().body(bookingService.getItemsBookingsOfUser(userId, state, from, size));
    }

    @PostMapping
    public ResponseEntity<BookingDto> addBookingRequest(@Valid @RequestBody BookingDto bookingDto,
                                                        @RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("Добавлен новый запрос: {}", bookingDto);
        return ResponseEntity.created(URI.create("http://localhost:8080/bookings"))
                .body(bookingService.addBooking(userId, bookingDto));
    }

    @GetMapping("/owner")
    public ResponseEntity<List<BookingDto>> getBookingByItemOwner(@RequestHeader("X-Sharer-User-Id") long userId,
                                                                  @RequestParam(defaultValue = "ALL") String state,
                                                                  @RequestParam(defaultValue = "0") @Min(0) Integer from,
                                                                  @RequestParam(defaultValue = "20") @Min(1) @Max(100) Integer size) {
        return ResponseEntity.ok().body(bookingService.getBookingByItemOwner(userId, state, from, size));
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<BookingDto> getBooking(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                 @PathVariable long bookingId) {

        return ResponseEntity.ok().body(bookingService.getBooking(userId, bookingId));
    }

    @PatchMapping("/{bookingId}")
    public ResponseEntity<BookingDto> approvedBooking(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                      @PathVariable long bookingId,
                                                      @RequestParam(name = "approved") boolean available) {
        log.info("Отправлен запрос на изменение статуса бронирования от владельца c id: {}", userId);
        var result = bookingService.approved(userId, bookingId, available);
        return ResponseEntity.ok().body(result);
    }
}