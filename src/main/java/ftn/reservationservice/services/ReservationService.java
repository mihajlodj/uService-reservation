package ftn.reservationservice.services;

import ftn.reservationservice.domain.entities.Reservation;
import ftn.reservationservice.domain.entities.ReservationStatus;
import ftn.reservationservice.repositories.RequestForReservationRepository;
import ftn.reservationservice.repositories.ReservationRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class ReservationService {

    private final RestService restService;

    private final ReservationRepository reservationRepository;

    public List<Reservation> getActiveReservationsForLodge(UUID lodgeId) {
        return reservationRepository.findByStatusAndLodgeId(ReservationStatus.ACTIVE, lodgeId);
    }

}
