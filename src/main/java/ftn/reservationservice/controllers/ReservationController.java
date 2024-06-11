package ftn.reservationservice.controllers;

import ftn.reservationservice.services.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

}
