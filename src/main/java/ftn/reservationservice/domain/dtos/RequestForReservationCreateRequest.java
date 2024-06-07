package ftn.reservationservice.domain.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;
import ftn.reservationservice.domain.entities.RequestForReservationStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
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
public class RequestForReservationCreateRequest {

    @NotNull(message = "LodgeId is required")
    private UUID lodgeId;

    @NotNull(message = "DateFrom can't be null")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSSSSSS")
    private LocalDateTime dateFrom;

    @NotNull(message = "DateTo can't be null")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSSSSSS")
    private LocalDateTime dateTo;

    @NotNull(message = "Number of guests is required")
    private int numberOfGuests;

}
