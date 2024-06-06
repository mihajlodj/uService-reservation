package ftn.reservationservice.controllers;

import ftn.reservationservice.domain.dtos.RequestForReservationCreateRequest;
import ftn.reservationservice.services.RequestForReservationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reservation/requestforreservation")
@RequiredArgsConstructor
public class RequestForReservationController {

    private final RequestForReservationService requestForReservationService;

    @PostMapping
    @PreAuthorize("hasAuthority('GUEST')")
    public ResponseEntity<?> createRequestForReservation(@RequestBody @Valid RequestForReservationCreateRequest request) {
        return ResponseEntity.ok(requestForReservationService.create(request));
    }

}
