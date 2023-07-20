package ru.practicum.shareit.item.dto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestResponseDtoItem;


@Mapper
public interface ItemMapper {
    ItemMapper INSTANCE = Mappers.getMapper(ItemMapper.class);

    @Mapping(target = "requestId", source = "source")
    ItemDto toItemDto(Item source);

    Item toItem(ItemDto itemDto);

    @Mapping(target = "requestId", source = "source")
    ItemRequestResponseDtoItem toItemRequestResponseDtoItem(Item source);

    static Long mapRequestId(Item source) {
        Long result = source.getRequest() == null ? 0 : source.getRequest().getId();
        return result;
    }
}