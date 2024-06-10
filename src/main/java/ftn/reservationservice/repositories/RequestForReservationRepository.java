package ftn.reservationservice.repositories;

import ftn.reservationservice.domain.entities.RequestForReservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RequestForReservationRepository extends JpaRepository<RequestForReservation, UUID>, QuerydslPredicateExecutor<RequestForReservation> {

    List<RequestForReservation> findByLodgeId(UUID lodgeId);

    List<RequestForReservation> findByOwnerId(UUID ownerId);

}
