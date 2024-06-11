package ftn.reservationservice.domain.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;
import ftn.reservationservice.domain.entities.ReservationStatus;
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
public class ReservationDto {

    private UUID id;
    private UUID lodgeId;
    private UUID guestId;
    private UUID ownerId;
    private double price;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSSSSSS")
    private LocalDateTime dateFrom;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSSSSSS")
    private LocalDateTime dateTo;
    private int numberOfGuests;
    private ReservationStatus status;

}
