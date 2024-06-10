package ftn.reservationservice.services;

import ftn.reservationservice.AuthPostgresIntegrationTest;
import ftn.reservationservice.domain.dtos.CanceledReservationsCountDto;
import ftn.reservationservice.repositories.ReservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.jdbc.Sql;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Sql("/sql/reservation2.sql")
public class ReservationServiceTest extends AuthPostgresIntegrationTest {

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private ReservationRepository reservationRepository;

    @MockBean
    private RestService restService;

    @BeforeEach
    public void setup() {
        authenticateHost();
    }


    @Test
    public void getCountCanceledReservationsByGuestNoCanceledReservations() {
        String guestId = "e49fcaa5-d45b-4556-9d91-13e58187fea6";

        CanceledReservationsCountDto response = reservationService.countCanceledReservationsByGuest(UUID.fromString(guestId));

        assertNotNull(response);
        assertEquals(0, response.getCount());

    }

    @Test
    public void getCountCanceledReservationsByGuestOneCanceledReservation() {
        String guestId = "e49fcaa5-d45b-4556-9d91-13e58187fef9";

        CanceledReservationsCountDto response = reservationService.countCanceledReservationsByGuest(UUID.fromString(guestId));

        assertNotNull(response);
        assertEquals(1, response.getCount());

    }

}
