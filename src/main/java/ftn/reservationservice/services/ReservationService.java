package ftn.reservationservice.services;

import ftn.reservationservice.domain.dtos.CanceledReservationsCountDto;
import ftn.reservationservice.domain.dtos.ReservationDto;
import ftn.reservationservice.domain.dtos.UserDto;
import ftn.reservationservice.domain.entities.RequestForReservation;
import ftn.reservationservice.domain.entities.Reservation;
import ftn.reservationservice.domain.entities.ReservationStatus;
import ftn.reservationservice.domain.mappers.ReservationMapper;
import ftn.reservationservice.exception.exceptions.NotFoundException;
import ftn.reservationservice.repositories.RequestForReservationRepository;
import ftn.reservationservice.repositories.ReservationRepository;
import ftn.reservationservice.utils.AuthUtils;
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

    public Reservation createReservation(RequestForReservation request) {
        Reservation reservation = ReservationMapper.INSTANCE.toReservation(request);
        reservation.setStatus(ReservationStatus.ACTIVE);
        return reservationRepository.save(reservation);
    }

    public CanceledReservationsCountDto countCanceledReservationsByGuest(UUID guestId) {
        long count = reservationRepository.countByGuestIdAndStatus(guestId, ReservationStatus.CANCELED);
        return CanceledReservationsCountDto.builder()
                .count(count)
                .build();
    }

    public List<ReservationDto> getMyReservationsHost() {
        UserDto host = getLoggedInUser();
        List<Reservation> reservations = reservationRepository.findByOwnerId(host.getId());
        return ReservationMapper.INSTANCE.toDto(reservations);
    }

    private UserDto getLoggedInUser() {
        UUID id = AuthUtils.getLoggedUserId();
        UserDto user = restService.getUserById(id);
        if (user == null) {
            throw new NotFoundException("User doesn't exist");
        }
        return user;
    }

    public List<ReservationDto> getMyReservationsGuest() {
        UserDto guest = getLoggedInUser();
        List<Reservation> reservations = reservationRepository.findByGuestId(guest.getId());
        return ReservationMapper.INSTANCE.toDto(reservations);
    }

}
