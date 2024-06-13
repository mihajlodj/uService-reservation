package ftn.reservationservice.repositories;

import ftn.reservationservice.domain.entities.RequestForReservation;
import ftn.reservationservice.domain.entities.Reservation;
import ftn.reservationservice.domain.entities.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, UUID>, QuerydslPredicateExecutor<Reservation> {

    List<Reservation> findByStatusAndLodgeId(ReservationStatus status, UUID lodgeId);

    long countByGuestIdAndStatus(UUID guestId, ReservationStatus status);

    List<Reservation> findByOwnerId(UUID ownerId);

    List<Reservation> findByGuestId(UUID guestId);

    List<Reservation> findByLodgeId(UUID lodgeId);

    @Query("SELECT r FROM Reservation r WHERE r.guestId = :guestId AND r.status = 'ACTIVE' AND r.dateFrom > :futureDate")
    List<Reservation> findActiveReservationsByGuestIdAndFutureDate(@Param("guestId") UUID guestId, @Param("futureDate") LocalDateTime futureDate);

    boolean existsByGuestIdAndLodgeId(UUID guestId, UUID lodgeId);

}
