package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.exception.ObjectNotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.CommentRepository;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.user.UserRepository;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;
    private final ItemRequestRepository itemRequestRepository;

    @Override
    public ItemDto addItem(long userId, ItemItemRequestDto itemDto) {
        Item item = ItemMapper.INSTANCE.toItem(itemDto);
        if (itemDto.getRequestId() > 0) {
            var request = itemRequestRepository.findById(itemDto.getRequestId());
            if (request.isEmpty()) {
                throw new ObjectNotFoundException("Такого запроса не существует!");
            }
            item.setRequest(request.get());
        }
        var userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            throw new ObjectNotFoundException("Такого пользователя не существует.");
        }
        var user = userOptional.get();
        item.setOwner(user);
        return ItemMapper.INSTANCE.toItemDto(itemRepository.save(item));
    }

    @Override
    public ItemDto updateItem(long userId, ItemItemRequestDto itemDto) {
        Item item = ItemMapper.INSTANCE.toItem(itemDto);
        var changeItem = itemRepository.findById(itemDto.getId());
        var currentItem = changeItem.get();

        if (currentItem == null) {
            throw new ObjectNotFoundException("Такой вещи не существует!");
        }
        if (userId != currentItem.getOwner().getId()) {
            throw new ObjectNotFoundException("id вещи пользователя не совпадают с id владелььца вещи");
        }
        item.setOwner(currentItem.getOwner());
        if (itemDto.getName() != null) {
            currentItem.setName(item.getName());
        }
        if (itemDto.getDescription() != null) {
            currentItem.setDescription(item.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            currentItem.setAvailable(item.isAvailable());
        }
        return ItemMapper.INSTANCE.toItemDto(itemRepository.save(currentItem));
    }

    @Override
    public ItemDto getItemById(long itemId, long userId) {
        var item = itemRepository.findById(itemId).orElseThrow(() ->
                new ObjectNotFoundException("Вещь не найдена!"));
        List<Comment> comments = commentRepository.findCommentsByItemId(itemId);
        var itemDto = ItemMapper.INSTANCE.toItemDto(item);
        List<CommentDto> commentsDto = new ArrayList<>();
        for (Comment comment : comments) {
            commentsDto.add(CommentMapper.INSTANCE.toCommentDto(comment));
        }
        itemDto.setComments(commentsDto);

        if (userId == item.getOwner().getId()) {
            var bookings = bookingRepository.findBookingByItemIdAndStatus(item.getId(), Status.APPROVED);
            if (bookings.size() != 0) {
                bookings = bookings.stream()
                        .sorted(Comparator.comparing(Booking::getStarts).reversed())
                        .collect(Collectors.toList());
                for (Booking booking : bookings) {
                    if (booking.getStarts().isBefore(LocalDateTime.now())) {
                        itemDto.setLastBooking(BookingMapper.INSTANCE.toBookingDto(booking));
                        break;
                    }
                }
                bookings = bookings.stream()
                        .sorted(Comparator.comparing(Booking::getStarts))
                        .collect(Collectors.toList());
                for (Booking booking : bookings) {
                    if (booking.getStarts().isAfter(LocalDateTime.now())) {
                        itemDto.setNextBooking(BookingMapper.INSTANCE.toBookingDto(booking));
                        break;
                    }
                }
            }
        }
        return itemDto;
    }

    @Override
    public List<ItemDto> getAllItemsByUserId(long userId) {
        List<ItemDto> itemsDto = new ArrayList<>();
        List<Item> items = itemRepository.findItemByOwnerId(userId).stream()
                .sorted(Comparator.comparingLong(Item::getId))
                .collect(Collectors.toList());;

        for (Item item : items) {
            var itemDto = ItemMapper.INSTANCE.toItemDto(item);
            if (userId == item.getOwner().getId()) {
                var bookings = bookingRepository.findBookingByItemIdAndStatus(item.getId(), Status.APPROVED);

                if (bookings.size() > 0) {
                    bookings = bookings.stream()
                            .sorted(Comparator.comparing(Booking::getStarts).reversed())
                            .collect(Collectors.toList());
                    for (Booking booking : bookings) {
                        if (booking.getStarts().isBefore(LocalDateTime.now())) {
                            itemDto.setLastBooking(BookingMapper.INSTANCE.toBookingDto(booking));
                            break;
                        }
                    }
                    bookings = bookings.stream()
                            .sorted(Comparator.comparing(Booking::getStarts))
                            .collect(Collectors.toList());
                    for (Booking booking : bookings) {
                        if (booking.getStarts().isAfter(LocalDateTime.now())) {
                            itemDto.setNextBooking(BookingMapper.INSTANCE.toBookingDto(booking));
                            break;
                        }
                    }
                }
            }
            List<Comment> comments = commentRepository.findCommentsByItemId(item.getId());
            List<CommentDto> commentsDto = new ArrayList<>();
            for (Comment comment : comments) {
                commentsDto.add(CommentMapper.INSTANCE.toCommentDto(comment));
            }
            itemDto.setComments(commentsDto);
            itemsDto.add(itemDto);
        }
        return itemsDto;
    }

    @Override
    public List<ItemDto> textSearch(String text) {
        List<ItemDto> itemsDto = new ArrayList<>();
        if (text.isBlank()) {
            return itemsDto;
        }
        List<Item> items = itemRepository.search(text);
        for (Item item : items) {
            if (item.isAvailable()) {
                itemsDto.add(ItemMapper.INSTANCE.toItemDto(item));
            }
        }
        return itemsDto;
    }

    @Override
    public CommentDto addComment(long userId, long itemId, CommentDto commentDto) {
        var itemOptional = itemRepository.findById(itemId);
        if (itemOptional.isEmpty()) {
            throw new ObjectNotFoundException("Такой вещи нет.");
        }
        var item = itemOptional.get();
        var userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            throw new ObjectNotFoundException("Такого пользователя не существует.");
        }
        var user = userOptional.get();
        List<Booking> bookings = bookingRepository.findBookingByItemId(itemId);
        boolean isExist = false;
        for (Booking booking : bookings) {
            if (booking.getBooker().getId() == userId
                    && booking.getStarts().isBefore(LocalDateTime.now())
                    && booking.getStatus().equals(Status.APPROVED)) {
                isExist = true;
                break;
            }
        }
        if (!isExist) {
            throw new ValidationException("Этой вещью не пользовался данный пользователь.");
        }
        Comment comment = CommentMapper.INSTANCE.toComment(commentDto);
        comment.setAuthor(user);
        comment.setItem(item);
        comment.setCreated(LocalDateTime.now());
        return CommentMapper.INSTANCE.toCommentDto(commentRepository.save(comment));
    }
}