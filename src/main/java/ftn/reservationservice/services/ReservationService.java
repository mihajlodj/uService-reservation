package ftn.reservationservice.services;

import ftn.reservationservice.domain.dtos.*;
import ftn.reservationservice.domain.entities.RequestForReservation;
import ftn.reservationservice.domain.entities.Reservation;
import ftn.reservationservice.domain.entities.ReservationStatus;
import ftn.reservationservice.domain.mappers.ReservationMapper;
import ftn.reservationservice.exception.exceptions.BadRequestException;
import ftn.reservationservice.exception.exceptions.ForbiddenException;
import ftn.reservationservice.exception.exceptions.NotFoundException;
import ftn.reservationservice.repositories.RequestForReservationRepository;
import ftn.reservationservice.repositories.ReservationRepository;
import ftn.reservationservice.utils.AuthUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class ReservationService {

    private final RestService restService;

    private final RequestForReservationService requestForReservationService;

    private final ReservationRepository reservationRepository;

    public ReservationService(RestService restService,
                              ReservationRepository reservationRepository,
                              @Lazy RequestForReservationService requestForReservationService) {
        this.restService = restService;
        this.reservationRepository = reservationRepository;
        this.requestForReservationService = requestForReservationService;
    }

    public List<Reservation> getActiveReservationsForLodge(UUID lodgeId) {
        return reservationRepository.findByStatusAndLodgeId(ReservationStatus.ACTIVE, lodgeId);
    }

    public Reservation createReservation(RequestForReservation request) {
        Reservation reservation = ReservationMapper.INSTANCE.toReservation(request);
        reservation.setStatus(ReservationStatus.ACTIVE);
        reservation.setRequestForReservationId(request.getId());
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

    public List<ReservationDto> getReservationsForLodge(UUID lodgeId) {
        UserDto host = getLoggedInUser();
        LodgeDto lodge = getLodge(lodgeId);
        if (!lodge.getOwnerId().equals(host.getId())) {
            throw new ForbiddenException("You can only get reservations for lodges you own.");
        }
        List<Reservation> reservations = reservationRepository.findByLodgeId(lodgeId);
        return ReservationMapper.INSTANCE.toDto(reservations);
    }

    private LodgeDto getLodge(UUID lodgeId) {
        LodgeDto lodge = restService.getLodgeById(lodgeId);
        if (lodge == null) {
            throw new NotFoundException("Lodge doesn't exist");
        }
        return lodge;
    }

    public ReservationDto getReservationByIdHost(UUID id) {
        UserDto host = getLoggedInUser();
        Reservation reservation = reservationRepository.findById(id).orElseThrow(() -> new NotFoundException("Reservation doesn't exist"));
        if (!reservation.getOwnerId().equals(host.getId())) {
            throw new ForbiddenException("You can only get reservation for lodges you own.");
        }
        return ReservationMapper.INSTANCE.toDto(reservation);
    }

    public ReservationDto getReservationByIdGuest(UUID id) {
        UserDto guest = getLoggedInUser();
        Reservation reservation = reservationRepository.findById(id).orElseThrow(() -> new NotFoundException("Reservation doesn't exist"));
        if (!reservation.getGuestId().equals(guest.getId())) {
            throw new ForbiddenException("You can only get reservation for lodges you made reservations for.");
        }
        return ReservationMapper.INSTANCE.toDto(reservation);
    }

    public List<ReservationDto> getAllReservationsForCancelation() {
        UserDto guest = getLoggedInUser();
        LocalDateTime futureDate = LocalDateTime.now().plusDays(1);
        List<Reservation> reservations = reservationRepository.findActiveReservationsByGuestIdAndFutureDate(guest.getId(), futureDate);
        return ReservationMapper.INSTANCE.toDto(reservations);
    }

    public ReservationDto cancelReservation(UUID reservationId) {
        UserDto guest = getLoggedInUser();
        Reservation reservation = reservationRepository.findById(reservationId).orElseThrow(() -> new NotFoundException("Reservation doesn't exist"));
        checkDidGuestMadeReservation(guest, reservation);
        checkReservationStatusIsACTIVE(reservation);
        checkIfDateRequirementsForCancelationAreMet(reservation);

        executeReservationCancelation(reservation);
        cancelReservationRequest(reservation.getRequestForReservationId());

        return ReservationMapper.INSTANCE.toDto(reservation);
    }

    private void checkDidGuestMadeReservation(UserDto guest, Reservation reservation) {
        if (!reservation.getGuestId().equals(guest.getId())) {
            throw new ForbiddenException("You can only cancel reservation you made.");
        }
    }

    private void checkReservationStatusIsACTIVE(Reservation reservation) {
        if (reservation.getStatus() != ReservationStatus.ACTIVE) {
            throw new BadRequestException("You can cancel only ACTIVE reservations.");
        }
    }

    private void checkIfDateRequirementsForCancelationAreMet(Reservation reservation) {
        // check if dateTime.now.plusOneDay is before reservation.getDateFrom
        LocalDateTime lastValidCancelationDate = getLastValidCancelationDate();
        if (!lastValidCancelationDate.isBefore(reservation.getDateFrom())) {
            throw new BadRequestException("You can cancel reservation only if start date is at least one day before reservation start.");
        }
    }

    private LocalDateTime getLastValidCancelationDate() {
        return LocalDateTime.now().plusDays(1);
    }

    private void executeReservationCancelation(Reservation reservation) {
        reservation.setStatus(ReservationStatus.CANCELED);
        reservationRepository.save(reservation);
    }

    private void cancelReservationRequest(UUID reservationRequestId) {
        requestForReservationService.cancelRequestForReservation(reservationRequestId);
    }

    public BoolCheckResponseDto checkIfUserHadReservationInLodge(UUID guestId, UUID lodgeId) {
        boolean exists = reservationRepository.existsByGuestIdAndLodgeId(guestId, lodgeId);
        return BoolCheckResponseDto.builder().value(exists).build();
    }

    public BoolCheckResponseDto checkIfUserHadReservationWithHost(UUID guestId, UUID hostId) {
        boolean exists = reservationRepository.existsByGuestIdAndOwnerId(guestId, hostId);
        return BoolCheckResponseDto.builder().value(exists).build();
    }

    public BoolCheckResponseDto checkIfReservationExistsInDateRange(UUID lodgeId, LocalDateTime dateFrom, LocalDateTime dateTo) {
        boolean exists = reservationRepository.existsByLodgeIdAndDateRangeOverlap(lodgeId, dateFrom, dateTo);
        return BoolCheckResponseDto.builder().value(exists).build();
    }

}
