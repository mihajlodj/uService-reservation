package ftn.reservationservice.domain.dtos;

import ftn.reservationservice.domain.entities.RequestForReservationStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestForReservationStatusUpdateRequest {

    @NotNull(message = "LodgeId is required")
    private RequestForReservationStatus status;

}
