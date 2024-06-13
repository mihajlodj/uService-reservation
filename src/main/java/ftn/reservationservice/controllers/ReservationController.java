package ftn.reservationservice.controllers;

import ftn.reservationservice.domain.dtos.RequestForReservationStatusUpdateRequest;
import ftn.reservationservice.services.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/reservation")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    @GetMapping("/canceled/count/{guestId}")
    @PreAuthorize("hasAuthority('HOST')")
    public ResponseEntity<?> countCanceledReservationsByGuest(@PathVariable UUID guestId) {
        return ResponseEntity.ok(reservationService.countCanceledReservationsByGuest(guestId));
    }

    @GetMapping("/all/host")
    @PreAuthorize("hasAuthority('HOST')")
    public ResponseEntity<?> getMyReservationsHost() {
        return ResponseEntity.ok(reservationService.getMyReservationsHost());
    }

    @GetMapping("/all/guest")
    @PreAuthorize("hasAuthority('GUEST')")
    public ResponseEntity<?> getMyReservationsGuest() {
        return ResponseEntity.ok(reservationService.getMyReservationsGuest());
    }

    @GetMapping("/all/{lodgeId}")
    @PreAuthorize("hasAuthority('HOST')")
    public ResponseEntity<?> getReservationsForLodge(@PathVariable UUID lodgeId) {
        return ResponseEntity.ok(reservationService.getReservationsForLodge(lodgeId));
    }

    @GetMapping("/host/{id}")
    @PreAuthorize("hasAuthority('HOST')")
    public ResponseEntity<?> getReservationByIdHost(@PathVariable UUID id) {
        return ResponseEntity.ok(reservationService.getReservationByIdHost(id));
    }

    @GetMapping("/guest/{id}")
    @PreAuthorize("hasAuthority('HOST')")
    public ResponseEntity<?> getReservationByIdGuest(@PathVariable UUID id) {
        return ResponseEntity.ok(reservationService.getReservationByIdGuest(id));
    }

    @GetMapping("/all/cancelation")
    @PreAuthorize("hasAuthority('GUEST')")
    public ResponseEntity<?> getAllReservationsForCancelation() {
        return ResponseEntity.ok(reservationService.getAllReservationsForCancelation());
    }

    @PutMapping("/cancellation/{id}")
    @PreAuthorize("hasAuthority('GUEST')")
    public ResponseEntity<?> cancelReservation(@PathVariable UUID id) {
        return ResponseEntity.ok(reservationService.cancelReservation(id));
    }

    @GetMapping("/check/userhadreservation/{guestId}/{lodgeId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<?> checkIfUserHadReservationInLodge(@PathVariable UUID guestId, @PathVariable UUID lodgeId) {
        return ResponseEntity.ok(reservationService.checkIfUserHadReservationInLodge(guestId, lodgeId));
    }

    @GetMapping("/check/userhadreservationwithhost/{guestId}/{hostId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<?> checkIfUserHadReservationWithHost(@PathVariable UUID guestId, @PathVariable UUID hostId) {
        return ResponseEntity.ok(reservationService.checkIfUserHadReservationWithHost(guestId, hostId));
    }

    @GetMapping("/check/reservationexistsindaterange/{lodgeId}/{dateFrom}/{dateTo}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<?> checkIfReservationExistsInDateRange(@PathVariable UUID lodgeId,
                                                                @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateFrom,
                                                                @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateTo) {
        return ResponseEntity.ok(reservationService.checkIfReservationExistsInDateRange(lodgeId, dateFrom, dateTo));
    }

}
