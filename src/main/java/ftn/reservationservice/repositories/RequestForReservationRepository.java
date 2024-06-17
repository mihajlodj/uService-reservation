package ftn.reservationservice.repositories;

import ftn.reservationservice.domain.entities.RequestForReservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface RequestForReservationRepository extends JpaRepository<RequestForReservation, UUID>, QuerydslPredicateExecutor<RequestForReservation> {

    List<RequestForReservation> findByLodgeId(UUID lodgeId);

    List<RequestForReservation> findByOwnerId(UUID ownerId);

    List<RequestForReservation> findByGuestId(UUID guestId);

    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END " +
            "FROM RequestForReservation r " +
            "WHERE r.lodgeId = :lodgeId " +
            "AND r.dateFrom <= :dateTo " +
            "AND r.dateTo >= :dateFrom")
    boolean existsByLodgeIdAndDateRangeOverlap(
            @Param("lodgeId") UUID lodgeId,
            @Param("dateFrom") LocalDateTime dateFrom,
            @Param("dateTo") LocalDateTime dateTo);

}
