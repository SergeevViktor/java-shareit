package ru.practicum.shareit.booking;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByBookerIdAndEndsIsBefore(Long bookerId, LocalDateTime ends, Sort sort);

    List<Booking> findByBookerIdAndStartsIsAfter(Long bookerId, LocalDateTime starts, Sort sort);

    List<Booking> findByBookerIdAndStatus(Long bookerId, Status status, Sort sort);

    List<Booking> findByBookerIdAndStartsIsBeforeAndEndsIsAfter(Long bookerId, LocalDateTime starts,
                                                                 LocalDateTime ends, Sort sort);

    List<Booking> findByItemOwnerIdAndEndsIsBefore(Long bookerId, LocalDateTime ends, Sort sort);

    List<Booking> findByItemOwnerIdAndStartsIsAfter(Long bookerId, LocalDateTime starts, Sort sort);

    List<Booking> findByItemOwnerIdAndStatus(Long bookerId, Status status, Sort sort);

    List<Booking> findByItemOwnerIdAndStartsIsBeforeAndEndsIsAfter(Long bookerId, LocalDateTime starts,
                                                                   LocalDateTime ends, Sort sort);

    List<Booking> findByBookerId(Long bookerId);

    List<Booking> findBookingByItemId(Long itemId);

    List<Booking> findBookingByItemIdAndStatus(Long itemId, Status status);

    List<Booking> findByItemOwnerIdOrderByStartsDesc(Long ownerId);
}
