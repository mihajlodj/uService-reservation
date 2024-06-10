package ftn.reservationservice.services;

import ftn.reservationservice.AuthPostgresIntegrationTest;
import ftn.reservationservice.domain.dtos.CanceledReservationsCountDto;
import ftn.reservationservice.domain.dtos.RequestForReservationDto;
import ftn.reservationservice.domain.dtos.ReservationDto;
import ftn.reservationservice.domain.dtos.UserDto;
import ftn.reservationservice.domain.entities.Reservation;
import ftn.reservationservice.exception.exceptions.NotFoundException;
import ftn.reservationservice.repositories.ReservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

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

    @Test
    public void testGetMyReservationsHostSuccess() {
        String lodgeOwnerId = "e49fcab5-d45b-4556-9d91-14e58177fea6";
        mockOwner(lodgeOwnerId);

        List<ReservationDto> response = reservationService.getMyReservationsHost();

        assertNotNull(response);
        assertEquals(2, response.size());
    }

    @Test
    public void testGetMyReservationsHostOwnerNotFound() {
        String lodgeOwnerId = "e49fcab5-d45b-4556-9d91-14e58177fea6";
        when(restService.getUserById(any(UUID.class))).thenReturn(null);

        assertThrows(NotFoundException.class, () -> reservationService.getMyReservationsHost());
    }

    @Test
    public void testGetMyReservationsGuestSuccess() {
        String guestId = "e49fcaa5-d45b-4556-9d91-13e58187fea6";
        mockGuest(guestId);

        List<ReservationDto> response = reservationService.getMyReservationsGuest();

        assertNotNull(response);
        assertEquals(1, response.size());
    }

    @Test
    public void testGetMyReservationsGuestNotFound() {
        when(restService.getUserById(any(UUID.class))).thenReturn(null);

        assertThrows(NotFoundException.class, () -> reservationService.getMyReservationsGuest());
    }

    private void mockGuest(String userId) {
        UserDto mockUserDTO = UserDto.builder()
                .id(UUID.fromString(userId))
                .role("GUEST")
                .build();

        when(restService.getUserById(any(UUID.class))).thenReturn(mockUserDTO);
    }

    private void mockOwner(String ownerId) {
        UserDto mockUserDTO = UserDto.builder()
                .id(UUID.fromString(ownerId))
                .role("HOST")
                .build();

        when(restService.getUserById(any(UUID.class))).thenReturn(mockUserDTO);
    }

}
