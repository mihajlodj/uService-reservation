package ftn.reservationservice.controllers;

import ftn.reservationservice.domain.dtos.RequestForReservationCreateRequest;
import ftn.reservationservice.domain.dtos.RequestForReservationStatusUpdateRequest;
import ftn.reservationservice.services.RequestForReservationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

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

    @DeleteMapping(value = "/{id}")
    @PreAuthorize("hasAuthority('GUEST')")
    public ResponseEntity<?> deleteRequestForReservation(@PathVariable UUID id) {
        requestForReservationService.delete(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('HOST')")
    public ResponseEntity<?> approveOrDenyRequest(@PathVariable UUID id, @RequestBody RequestForReservationStatusUpdateRequest updateRequest) {
        return ResponseEntity.ok(requestForReservationService.update(id, updateRequest));
    }

    @GetMapping("/all/host")
    @PreAuthorize("hasAuthority('HOST')")
    public ResponseEntity<?> getHostReservationRequests() {
        return ResponseEntity.ok(requestForReservationService.getHostReservationRequests());
    }

    @GetMapping("/all/guest")
    @PreAuthorize("hasAuthority('GUEST')")
    public ResponseEntity<?> getGuestReservationRequests() {
        return ResponseEntity.ok(requestForReservationService.getGuestReservationRequests());
    }

    @GetMapping("/host/{id}")
    @PreAuthorize("hasAuthority('HOST')")
    public ResponseEntity<?> getReservationRequestByIdHost(@PathVariable UUID id) {
        return ResponseEntity.ok(requestForReservationService.getReservationRequestByIdHost(id));
    }

    @GetMapping("/guest/{id}")
    @PreAuthorize("hasAuthority('GUEST')")
    public ResponseEntity<?> getReservationRequestByIdGuest(@PathVariable UUID id) {
        return ResponseEntity.ok(requestForReservationService.getReservationRequestByIdGuest(id));
    }

    @GetMapping("/check/requestforreservationexists/{lodgeId}/{dateFrom}/{dateTo}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<?> checkIfRequestForReservationExists(@PathVariable UUID lodgeId,
                                                            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateFrom,
                                                            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateTo) {
        return ResponseEntity.ok(requestForReservationService.checkIfRequestForReservationExists(lodgeId, dateFrom, dateTo));
    }

}
