package ru.practicum.shareit.request.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ItemRequestResponseDtoItem {
    private Long id;
    private String name;
    private String description;
    private Long requestId;
    private Boolean available;
}
