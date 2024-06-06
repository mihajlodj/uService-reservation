package ftn.reservationservice.domain.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LodgeDto {

    private UUID id;
    private UUID ownerId;
    private String name;
    private String location;
    private int minimalGuestNumber;
    private int maximalGuestNumber;
    private String approvalType;

}
