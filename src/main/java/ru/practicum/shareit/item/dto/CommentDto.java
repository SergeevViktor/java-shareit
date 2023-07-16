package ru.practicum.shareit.item.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CommentDto {
    private long id;
    private String text;
    private long itemId;
    private long authorId;
    private String authorName;
    private LocalDateTime created;
}