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

}