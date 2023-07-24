package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.State;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.exceptions.ObjectNotFoundException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.exceptions.WrongStatusException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.dto.UserMapper;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {
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
            throw new ObjectNotFoundException("Зачем самому себе брать вещь в аренду! :)");
        }
        booking.setBooker(user);
        booking.setItem(item);
        booking.setStatus(Status.WAITING);
        log.info("Добавлна новый запрос от пользователя; {}", booking.getBooker().getName());
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
            throw new ObjectNotFoundException("id вещи пользователя не совпадают с id владелььца вещи");
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
    public List<BookingDto> getItemsBookingsOfUser(long userId, String state, int from, int size) {
        if (from < 0) {
            throw new ValidationException("From не может быть отрицальным!");
        }
        PageRequest page = PageRequest.of(from / size, size);
        PageRequest pageWithSort = PageRequest.of(from / size, size, sort);
        State stateEnum;
        try {
            stateEnum = State.valueOf(state);
        } catch (Exception ex) {
            throw new WrongStatusException("Unknown state: UNSUPPORTED_STATUS");
        }
        var user = userRepository.findById(userId);
        if (user.isEmpty()) {
            throw new ObjectNotFoundException("Пользователь не найден");
        }
        List<Booking> bookings;
        LocalDateTime time = LocalDateTime.now();
        switch (stateEnum) {
            case PAST:
                bookings = bookingRepository.findByBookerIdAndEndsIsBefore(userId, time, pageWithSort);
                break;
            case FUTURE:
                bookings = bookingRepository.findByBookerIdAndStartsIsAfter(userId, time, pageWithSort);
                break;
            case CURRENT:
                bookings = bookingRepository.findByBookerIdAndStartsIsBeforeAndEndsIsAfter(userId,
                        time, time, pageWithSort);
                break;
            case WAITING:
                bookings = bookingRepository.findByBookerIdAndStatus(userId, Status.WAITING, pageWithSort);
                break;
            case REJECTED:
                bookings = bookingRepository.findByBookerIdAndStatus(userId, Status.REJECTED, pageWithSort);
                break;
            default:
                bookings = bookingRepository.findByBookerIdOrderByStartsDesc(userId, page);
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
    public List<BookingDto> getBookingByItemOwner(long userId, String state, int from, int size) {
        if (from < 0) {
            throw new ValidationException("From не может быть отрицальным!");
        }
        PageRequest page = PageRequest.of(from / size, size);
        PageRequest pageWithSort = PageRequest.of(from / size, size, sort);
        State stateEnum;
        try {
            stateEnum = State.valueOf(state);
        } catch (Exception ex) {
            throw new WrongStatusException("Unknown state: UNSUPPORTED_STATUS");
        }
        var user = userRepository.findById(userId);
        if (user.isEmpty()) {
            throw new ObjectNotFoundException("Пользователь не найден");
        }
        List<Booking> bookings;
        LocalDateTime time = LocalDateTime.now();
        switch (stateEnum) {
            case PAST:
                bookings = bookingRepository.findByItemOwnerIdAndEndsIsBefore(userId, time, pageWithSort);
                break;
            case FUTURE:
                bookings = bookingRepository.findByItemOwnerIdAndStartsIsAfter(userId, time, pageWithSort);
                break;
            case CURRENT:
                bookings = bookingRepository.findByItemOwnerIdAndStartsIsBeforeAndEndsIsAfter(userId,
                        time, time, pageWithSort);
                break;
            case WAITING:
                bookings = bookingRepository.findByItemOwnerIdAndStatus(userId, Status.WAITING, pageWithSort);
                break;
            case REJECTED:
                bookings = bookingRepository.findByItemOwnerIdAndStatus(userId, Status.REJECTED, pageWithSort);
                break;
            default:
                bookings = bookingRepository.findByItemOwnerIdOrderByStartsDesc(userId, page);
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
