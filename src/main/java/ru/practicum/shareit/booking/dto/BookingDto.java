package ru.practicum.shareit.booking.dto;

import lombok.Data;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;

/**
 * TODO Sprint add-bookings.
 */

@Data
public class BookingDto {
    private Long id;
    private LocalDateTime start;
    private LocalDateTime end;
    private ItemDto item;
    private long itemId;
    private UserDto booker;
    private long bookerId;
    private Status status;
}
