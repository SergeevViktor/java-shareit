package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.State;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.exceptions.ObjectNotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.dto.UserMapper;

import javax.validation.ValidationException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService{
    private final BookingRepository bookingRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final Sort sort = Sort.by(Sort.Direction.DESC, "starts");

    @Override
    public BookingDto addBooking(long userId, BookingDto bookingDto) {
        validateTimeBooking(bookingDto);
        var booking = BookingMapper.INSTANCE.toBooking(bookingDto);
        var userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            throw new ObjectNotFoundException("Пользователь не найден");
        }
        var user = userOptional.get();
        var itemOptional = itemRepository.findById(bookingDto.getItemId());
        if (itemOptional.isEmpty()) {
            throw new ObjectNotFoundException("Вещь не существует");
        }
        var item = itemOptional.get();
        if (!item.isAvailable()) {
            throw new ValidationException("Данная вещь не доступна для бронирования!");
        }
        if (item.getOwner().getId() == userId) {
            throw new ObjectNotFoundException("Вещь ваша - это не имеет смысла!");
        }
        booking.setBooker(user);
        booking.setItem(item);
        booking.setStatus(Status.WAITING);
        log.info("Добавлен новый запрос от пользователя; {}", booking.getBooker().getName());
        var bookingTemp = bookingRepository.save(booking);
        var result = BookingMapper.INSTANCE.toBookingDto(bookingTemp);
        result.setItem(ItemMapper.INSTANCE.toItemDto(item));
        result.setBooker(UserMapper.INSTANCE.toUserDto(user));
        return result;
    }

    @Override
    public BookingDto approved(long userId, long bookingId, boolean available) {
        var booking = bookingRepository.findById(bookingId);
        if (booking.isEmpty()) {
            throw new ObjectNotFoundException("Такого бронирования не существует!");
        }
        if (booking.get().getItem().getOwner().getId() != (userId)) {
            throw new ObjectNotFoundException("Id вещи пользователя не совпадают с id владелььца вещи");
        }
        Status status = booking.get().getStatus();
        if (!status.equals(Status.WAITING)) {
            throw new ValidationException("Статус нельзя изменить!");
        }
        if (available) {
            booking.get().setStatus(Status.APPROVED);
        } else {
            booking.get().setStatus(Status.REJECTED);
        }
        var result = BookingMapper.INSTANCE.toBookingDto(bookingRepository.save(booking.get()));
        result.setItem(ItemMapper.INSTANCE.toItemDto(booking.get().getItem()));
        result.setBooker(UserMapper.INSTANCE.toUserDto(booking.get().getBooker()));
        return result;
    }

    @Override
    public BookingDto getBooking(long userId, long bookingId) {
        var booking = bookingRepository.findById(bookingId);
        if (booking.isEmpty()) {
            throw new ObjectNotFoundException("Такого бронирования не существует!");
        }
        if (booking.get().getBooker().getId() != (userId) &&
                booking.get().getItem().getOwner().getId() != (userId)) {
            throw new ObjectNotFoundException("Данные бронирования открыты автору бронирования или владельцу вещи!");
        }
        var result = BookingMapper.INSTANCE.toBookingDto(bookingRepository.findById(bookingId).get());
        result.setItem(ItemMapper.INSTANCE.toItemDto(booking.get().getItem()));
        result.setBooker(UserMapper.INSTANCE.toUserDto(booking.get().getBooker()));
        return result;
    }

    @Override
    public List<BookingDto> getItemsBookingsOfUser(long userId, State state) {
        var user = userRepository.findById(userId);
        if (user.isEmpty()) {
            throw new ObjectNotFoundException("Пользователь не найден");
        }
        List<Booking> bookings = bookingRepository.findByBookerId(userId);
        LocalDateTime time = LocalDateTime.now();
        switch (state) {
            case PAST:
                bookings = bookingRepository.findByBookerIdAndEndsIsBefore(userId, time, sort);
                break;
            case FUTURE:
                bookings = bookingRepository.findByBookerIdAndStartsIsAfter(userId, time, sort);
                break;
            case CURRENT:
                bookings = bookingRepository.findByBookerIdAndStartsIsBeforeAndEndsIsAfter(userId,
                        time, time, sort);
                break;
            case WAITING:
                bookings = bookingRepository.findByBookerIdAndStatus(userId, Status.WAITING, sort);
                break;
            case REJECTED:
                bookings = bookingRepository.findByBookerIdAndStatus(userId, Status.REJECTED, sort);
                break;
            default:
                bookings.sort((booking1, booking2) -> booking2.getStarts().compareTo(booking1.getStarts()));
                break;
        }
        List<BookingDto> result = new ArrayList<>();
        for (Booking booking : bookings) {
            var bookingDto = BookingMapper.INSTANCE.toBookingDto(booking);
            bookingDto.setItem(ItemMapper.INSTANCE.toItemDto(booking.getItem()));
            bookingDto.setBooker(UserMapper.INSTANCE.toUserDto(booking.getBooker()));
            result.add(bookingDto);
        }
        return result;
    }

    @Override
    public List<BookingDto> getBookingByItemOwner(long userId, State state) {
        var user = userRepository.findById(userId);
        if (user.isEmpty()) {
            throw new ObjectNotFoundException("Пользователь не найден");
        }
        List<Booking> bookings = bookingRepository.findByItemOwnerIdOrderByStartsDesc(userId);
        LocalDateTime time = LocalDateTime.now();
        switch (state) {
            case PAST:
                bookings = bookingRepository.findByItemOwnerIdAndEndsIsBefore(userId, time, sort);
                break;
            case FUTURE:
                bookings = bookingRepository.findByItemOwnerIdAndStartsIsAfter(userId, time, sort);
                break;
            case CURRENT:
                bookings = bookingRepository.findByItemOwnerIdAndStartsIsBeforeAndEndsIsAfter(userId,
                        time, time, sort);
                break;
            case WAITING:
                bookings = bookingRepository.findByItemOwnerIdAndStatus(userId, Status.WAITING, sort);
                break;
            case REJECTED:
                bookings = bookingRepository.findByItemOwnerIdAndStatus(userId, Status.REJECTED, sort);
                break;
        }
        List<BookingDto> result = new ArrayList<>();
        for (Booking booking : bookings) {
            var bookingDto = BookingMapper.INSTANCE.toBookingDto(booking);
            bookingDto.setItem(ItemMapper.INSTANCE.toItemDto(booking.getItem()));
            bookingDto.setBooker(UserMapper.INSTANCE.toUserDto(booking.getBooker()));
            result.add(bookingDto);
        }
        return result;
    }

    private void validateTimeBooking(BookingDto bookingDto) {
        if (bookingDto.getStart() == null || bookingDto.getEnd() == null) {
            throw new ValidationException("Поля не могут быть пустыми");
        }
        if (bookingDto.getStart().isEqual(bookingDto.getEnd())) {
            throw new ValidationException("Дата начала бронирования не может совпадать с датой окончания!");
        }
        if (bookingDto.getEnd().isBefore(LocalDateTime.now().minusMinutes(1))) {
            throw new ValidationException("Дата окончания бронирования не может быть в прошлом!!");
        }
        if (bookingDto.getStart().isBefore(LocalDateTime.now().minusMinutes(1))) {
            throw new ValidationException("Дата начала бронирования не может быть раньше текущего момента!");
        }
        if (bookingDto.getStart().isAfter(bookingDto.getEnd())) {
            throw new ValidationException("Дата начала бронирования не может быть позднее даты окончания бронирования!");
        }
    }
}
