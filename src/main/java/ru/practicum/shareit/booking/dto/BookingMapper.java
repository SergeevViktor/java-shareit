package ru.practicum.shareit.booking.dto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import ru.practicum.shareit.booking.model.Booking;

@Mapper
public interface BookingMapper {
    BookingMapper INSTANCE = Mappers.getMapper(BookingMapper.class);

    @Mapping(target = "start", source = "booking.starts")
    @Mapping(target = "end", source = "booking.ends")
    @Mapping(target = "itemId", source = "booking.item.id")
    @Mapping(target = "bookerId", source = "booking.booker.id")
    BookingDto toBookingDto(Booking booking);

    @Mapping(target = "starts", source = "bookingDto.start")
    @Mapping(target = "ends", source = "bookingDto.end")
    Booking toBooking(BookingDto bookingDto);
}