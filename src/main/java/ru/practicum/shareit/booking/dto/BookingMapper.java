package ru.practicum.shareit.booking.dto;

import lombok.experimental.UtilityClass;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import ru.practicum.shareit.booking.model.Booking;

//@Mapper
@UtilityClass
public class BookingMapper {
    //BookingMapper INSTANCE = Mappers.getMapper(BookingMapper.class);

    //Booking toBooking(BookingDto bookingDto);

    //BookingDto toBookingDto(Booking booking);

    //BookingResponseDto toBookingResponseDto(Booking booking);

    public static BookingDto toBookingDto(Booking booking) {
        return BookingDto.builder()
                .id(booking.getId())
                .start(booking.getStarts())
                .end(booking.getEnds())
                .status(booking.getStatus())
                .itemId(booking.getItem().getId())
                .bookerId(booking.getBooker().getId())
                .build();
    }

    public static Booking toBooking(BookingDto bookingDto) {
        return Booking.builder()
                .id(bookingDto.getId())
                .starts(bookingDto.getStart())
                .ends(bookingDto.getEnd())
                .status(bookingDto.getStatus())
                .build();
    }
}