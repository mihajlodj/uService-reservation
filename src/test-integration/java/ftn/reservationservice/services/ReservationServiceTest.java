package ftn.reservationservice.services;

import ftn.reservationservice.AuthPostgresIntegrationTest;
import ftn.reservationservice.domain.dtos.*;
import ftn.reservationservice.domain.entities.RequestForReservationStatus;
import ftn.reservationservice.domain.entities.Reservation;
import ftn.reservationservice.domain.entities.ReservationStatus;
import ftn.reservationservice.exception.exceptions.ForbiddenException;
import ftn.reservationservice.exception.exceptions.NotFoundException;
import ftn.reservationservice.repositories.ReservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.jdbc.Sql;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
        assertEquals(2, response.getCount());

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

    @Test
    public void testGetReservationsForLodgeSuccess() {
        String lodgeId = "b86553e1-2552-41cb-9e40-7ef87c424850";
        String lodgeOwnerId = "e49fcab5-d45b-4556-9d91-14e58177fea6";

        mockOwner(lodgeOwnerId);
        mockLodgeAutomaticApproval(lodgeId, lodgeOwnerId);

        List<ReservationDto> response = reservationService.getReservationsForLodge(UUID.fromString(lodgeId));

        assertNotNull(response);
        assertEquals(2, response.size());
    }

    @Test
    public void testGetReservationsForLodgeOwnerNotFound() {
        String lodgeId = "b86553e1-2552-41cb-9e40-7ef87c424850";
        String lodgeOwnerId = "e49fcab5-d45b-4556-9d91-14e58177fea6";

        when(restService.getUserById(any(UUID.class))).thenReturn(null);
        mockLodgeAutomaticApproval(lodgeId, lodgeOwnerId);

        assertThrows(NotFoundException.class, () -> reservationService.getReservationsForLodge(UUID.fromString(lodgeId)));
    }

    @Test
    public void testGetReservationsForLodgeNotFound() {
        String lodgeId = "b86553e1-2552-41cb-9e40-7ef87c424850";
        String lodgeOwnerId = "e49fcab5-d45b-4556-9d91-14e58177fea6";

        mockOwner(lodgeOwnerId);
        when(restService.getLodgeById(any(UUID.class))).thenReturn(null);

        assertThrows(NotFoundException.class, () -> reservationService.getReservationsForLodge(UUID.fromString(lodgeId)));
    }

    @Test
    public void testGetReservationsForLodgeThatOwnerDoesntOwn() {
        String lodgeId = "b86553e1-2552-41cb-9e40-7ef87c424859";
        String lodgeOwnerId = "e49fcab5-d45b-4556-9d91-14e58177fea6";
        String fakeOwnerId = "e49fcab5-d45b-4556-9d92-14e58177fea6";

        mockOwner(lodgeOwnerId);
        mockLodgeAutomaticApproval(lodgeId, fakeOwnerId);

        assertThrows(ForbiddenException.class, () -> reservationService.getReservationsForLodge(UUID.fromString(lodgeId)));
    }

    @Test
    public void testGetReservationByIdHostSuccess() {
        String lodgeOwnerId = "e49fcab5-d45b-4556-9d91-14e58177fea6";
        mockOwner(lodgeOwnerId);

        String reservationId = "b86553e1-2552-41cb-9e40-7aaaaa424850";

        ReservationDto response = reservationService.getReservationByIdHost(UUID.fromString(reservationId));

        assertNotNull(response);
        assertEquals(UUID.fromString(reservationId), response.getId());
        assertEquals(UUID.fromString("b86553e1-2552-41cb-9e40-7ef87c424850"), response.getLodgeId());
        assertEquals(UUID.fromString("e49fcaa5-d45b-4556-9d91-13e58187fea6"), response.getGuestId());
        assertEquals(UUID.fromString(lodgeOwnerId), response.getOwnerId());
        assertEquals(99.99, response.getPrice());
        assertEquals(LocalDateTime.parse("2024-05-19 00:00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")), response.getDateFrom());
        assertEquals(LocalDateTime.parse("2024-05-23 00:00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")), response.getDateTo());
        assertEquals(2, response.getNumberOfGuests());
        assertEquals(ReservationStatus.ACTIVE, response.getStatus());

    }

    @Test
    public void testGetReservationByIdHostNotFound() {
        String lodgeOwnerId = "e49fcab5-d45b-4556-9d91-14e58177fea6";
        when(restService.getUserById(any(UUID.class))).thenReturn(null);

        String reservationId = "b86553e1-2552-41cb-9e40-7aaaaa424850";

        assertThrows(NotFoundException.class, () -> reservationService.getReservationByIdHost(UUID.fromString(reservationId)));
    }

    @Test
    public void testGetReservationByIdHostReservationNotFound() {
        String lodgeOwnerId = "e49fcab5-d45b-4556-9d91-14e58177fea6";
        mockOwner(lodgeOwnerId);

        String reservationId = "b86553e1-2552-41cb-9e40-7aaaaa424853";

        assertThrows(NotFoundException.class, () -> reservationService.getReservationByIdHost(UUID.fromString(reservationId)));
    }

    @Test
    public void testGetReservationByIdHostThatIsNotOwner() {
        String lodgeOwnerId = "e49fcab5-d45b-4556-9d91-14e58177fea6";
        mockOwner(lodgeOwnerId);

        String reservationId = "b86553e1-2552-41cb-9e40-7aaaaa424852";

        assertThrows(ForbiddenException.class, () -> reservationService.getReservationByIdHost(UUID.fromString(reservationId)));
    }

    @Test
    public void testGetReservationByIdGuestSuccess() {
        String guestId = "e49fcaa5-d45b-4556-9d91-13e58187fea6";
        mockGuest(guestId);

        String lodgeOwnerId = "e49fcab5-d45b-4556-9d91-14e58177fea6";
        String reservationId = "b86553e1-2552-41cb-9e40-7aaaaa424850";

        ReservationDto response = reservationService.getReservationByIdGuest(UUID.fromString(reservationId));

        assertNotNull(response);
        assertEquals(UUID.fromString(reservationId), response.getId());
        assertEquals(UUID.fromString("b86553e1-2552-41cb-9e40-7ef87c424850"), response.getLodgeId());
        assertEquals(UUID.fromString("e49fcaa5-d45b-4556-9d91-13e58187fea6"), response.getGuestId());
        assertEquals(UUID.fromString(lodgeOwnerId), response.getOwnerId());
        assertEquals(99.99, response.getPrice());
        assertEquals(LocalDateTime.parse("2024-05-19 00:00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")), response.getDateFrom());
        assertEquals(LocalDateTime.parse("2024-05-23 00:00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")), response.getDateTo());
        assertEquals(2, response.getNumberOfGuests());
        assertEquals(ReservationStatus.ACTIVE, response.getStatus());

    }

    @Test
    public void testGetReservationByIdGuestNotFound() {
        when(restService.getUserById(any(UUID.class))).thenReturn(null);

        String reservationId = "b86553e1-2552-41cb-9e40-7aaaaa424850";

        assertThrows(NotFoundException.class, () -> reservationService.getReservationByIdGuest(UUID.fromString(reservationId)));
    }

    @Test
    public void testGetReservationByIdGuestReservationNotFound() {
        String guestId = "e49fcaa5-d45b-4556-9d91-13e58187fea6";
        mockGuest(guestId);

        String reservationId = "b86553e1-2552-41cb-9e40-7aaaaa424853";

        assertThrows(NotFoundException.class, () -> reservationService.getReservationByIdGuest(UUID.fromString(reservationId)));
    }

    @Test
    public void testGetReservationByIdGuestThatDidntMadeReservation() {
        String guestId = "e49fcaa5-d45b-4556-9d91-13e58187fea6";
        mockGuest(guestId);

        String reservationId = "b86553e1-2552-41cb-9e40-7aaaaa424852";

        assertThrows(ForbiddenException.class, () -> reservationService.getReservationByIdGuest(UUID.fromString(reservationId)));
    }

    @Test
    public void testCheckIfUserHadReservationInLodgeSuccess() {
        authenticateAdmin();
        UUID guestId = UUID.fromString("e49fcaa5-d45b-4556-9d91-13e58187fea6");
        UUID lodgeId = UUID.fromString("b86553e1-2552-41cb-9e40-7ef87c424850");

        BoolCheckResponseDto response = reservationService.checkIfUserHadReservationInLodge(guestId, lodgeId);

        assertNotNull(response);
        assertTrue(response.isValue());
    }

    @Test
    public void testCheckIfUserHadReservationInLodgeGuestNotFound() {
        authenticateAdmin();
        UUID guestId = UUID.fromString("e49fcaa5-d45b-4556-9d91-13e58187fea7");
        UUID lodgeId = UUID.fromString("b86553e1-2552-41cb-9e40-7ef87c424850");

        BoolCheckResponseDto response = reservationService.checkIfUserHadReservationInLodge(guestId, lodgeId);

        assertNotNull(response);
        assertFalse(response.isValue());
    }

    @Test
    public void testCheckIfUserHadReservationInLodgeLodgeNotFound() {
        authenticateAdmin();
        UUID guestId = UUID.fromString("e49fcaa5-d45b-4556-9d91-13e58187fea6");
        UUID lodgeId = UUID.fromString("b86553e1-2552-41cb-9e40-7ef87c424857");

        BoolCheckResponseDto response = reservationService.checkIfUserHadReservationInLodge(guestId, lodgeId);

        assertNotNull(response);
        assertFalse(response.isValue());
    }

    @Test
    public void testCheckIfUserHadReservationWithHostSuccess() {
        authenticateAdmin();
        UUID guestId = UUID.fromString("e49fcaa5-d45b-4556-9d91-13e58187fea6");
        UUID hostId = UUID.fromString("e49fcab5-d45b-4556-9d91-14e58177fea6");

        BoolCheckResponseDto response = reservationService.checkIfUserHadReservationWithHost(guestId, hostId);

        assertNotNull(response);
        assertTrue(response.isValue());
    }

    @Test
    public void testCheckIfUserHadReservationWithHostGuestNotFound() {
        authenticateAdmin();
        UUID guestId = UUID.fromString("e49fcaa5-d45b-4556-9d91-13e58187fea3");
        UUID hostId = UUID.fromString("e49fcab5-d45b-4556-9d91-14e58177fea6");

        BoolCheckResponseDto response = reservationService.checkIfUserHadReservationWithHost(guestId, hostId);

        assertNotNull(response);
        assertFalse(response.isValue());
    }

    @Test
    public void testCheckIfUserHadReservationWithHostHostNotFound() {
        authenticateAdmin();
        UUID guestId = UUID.fromString("e49fcaa5-d45b-4556-9d91-13e58187fea6");
        UUID hostId = UUID.fromString("e49fcab5-d45b-4556-9d91-14e58177fea3");

        BoolCheckResponseDto response = reservationService.checkIfUserHadReservationWithHost(guestId, hostId);

        assertNotNull(response);
        assertFalse(response.isValue());
    }

    @Test
    public void testCheckIfReservationExistsInDateRangeSuccess() {
        UUID lodgeId = UUID.fromString("b86553e1-2552-41cb-9e40-7ef87c424850");
        LocalDateTime dateFrom = LocalDateTime.parse("2024-05-19T00:00:00");
        LocalDateTime dateTo = LocalDateTime.parse("2024-05-23T00:00:00");

        BoolCheckResponseDto response = reservationService.checkIfReservationExistsInDateRange(lodgeId, dateFrom, dateTo);

        assertNotNull(response);
        assertTrue(response.isValue());

    }

    @Test
    public void testCheckIfReservationExistsInDateRangeWhenItDoesnt() {
        UUID lodgeId = UUID.fromString("b86553e1-2552-41cb-9e40-7ef87c424850");
        LocalDateTime dateFrom = LocalDateTime.parse("2024-04-19T00:00:00");
        LocalDateTime dateTo = LocalDateTime.parse("2024-04-23T00:00:00");

        BoolCheckResponseDto response = reservationService.checkIfReservationExistsInDateRange(lodgeId, dateFrom, dateTo);

        assertNotNull(response);
        assertFalse(response.isValue());

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

    private void mockLodgeAutomaticApproval(String lodgeId, String lodgeOwnerId) {
        LodgeDto mockLodgeDTO = LodgeDto.builder()
                .id(UUID.fromString(lodgeId))
                .ownerId(UUID.fromString(lodgeOwnerId))
                .name("Vikendica")
                .location("Lokacija1")
                .minimalGuestNumber(1)
                .maximalGuestNumber(3)
                .approvalType("AUTOMATIC")
                .build();

        when(restService.getLodgeById(any(UUID.class))).thenReturn(mockLodgeDTO);
    }

}
