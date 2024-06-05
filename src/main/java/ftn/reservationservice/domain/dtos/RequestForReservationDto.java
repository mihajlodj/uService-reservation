package ftn.reservationservice.domain.dtos;

import ftn.reservationservice.domain.entities.RequestForReservationStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestForReservationDto {

    private UUID id;
    private UUID lodgeId;
    private UUID guestId;
    private double price;
    private LocalDateTime dateFrom;
    private LocalDateTime dateTo;
    private int numberOfGuests;
    private RequestForReservationStatus status;

}
