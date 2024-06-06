package ftn.reservationservice.domain.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "request_for_reservation")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RequestForReservation {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(nullable = false)
    private UUID lodgeId;

    @Column(nullable = false)
    private UUID guestId;

    @Column
    private double price;

    @Column(nullable = false)
    private LocalDateTime dateFrom;

    @Column(nullable = false)
    private LocalDateTime dateTo;

    @Column(nullable = false)
    private int numberOfGuests;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private RequestForReservationStatus status = RequestForReservationStatus.WAITING_FOR_RESPONSE;

}
