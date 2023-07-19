package ru.practicum.shareit.booking.dto;

import lombok.Data;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;

@Data
public class BookingResponseDto {
    private Long id;
    private ItemDto item;
    private long itemId;
    private UserDto booker;
    private Status status;
    private LocalDateTime start;
    private LocalDateTime end;
}
