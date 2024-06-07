package ftn.reservationservice.services;

import ftn.reservationservice.AuthPostgresIntegrationTest;
import ftn.reservationservice.domain.dtos.*;
import ftn.reservationservice.domain.entities.RequestForReservationStatus;
import ftn.reservationservice.exception.exceptions.BadRequestException;
import ftn.reservationservice.exception.exceptions.ForbiddenException;
import ftn.reservationservice.exception.exceptions.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.jdbc.Sql;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@Sql("/sql/reservation.sql")
public class RequestForReservationServiceTest extends AuthPostgresIntegrationTest {

    @Autowired
    private RequestForReservationService requestForReservationService;

    @MockBean
    private RestService restService;

    @BeforeEach
    public void setup() {
        authenticateGuest();
    }

    @Test
    public void testCreateRequestForReservationSuccessful() {
        String userId = "e49fcaa5-d45b-4556-9d91-13e58187fea6";
        mockGuest(userId);

        String lodgeId = "b86553e1-2552-41cb-9e40-7ef87c424850";
        String lodgeOwnerId = "e49fcab5-d45b-4556-9d91-14e58177fea6";
        mockLodge(lodgeId, lodgeOwnerId);

        mockLodgeAvailabilityPeriods(lodgeId);

        RequestForReservationCreateRequest request = RequestForReservationCreateRequest.builder()
                .lodgeId(UUID.fromString(lodgeId))
                .dateFrom(LocalDateTime.parse("2024-05-02 20:10:21.2632212", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSSS")))
                .dateTo(LocalDateTime.parse("2024-05-04 20:10:21.2632212", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSSS")))
                .numberOfGuests(2)
                .build();

        RequestForReservationDto response = requestForReservationService.create(request);

        assertNotNull(response);
        assertEquals(request.getLodgeId(), response.getLodgeId());
        assertEquals(UUID.fromString(userId), response.getGuestId());
        assertEquals(UUID.fromString(lodgeOwnerId), response.getOwnerId());
        assertEquals(120.0, response.getPrice());
        assertEquals(request.getDateFrom(), response.getDateFrom());
        assertEquals(request.getDateTo(), response.getDateTo());
        assertEquals(request.getNumberOfGuests(), response.getNumberOfGuests());
        assertEquals(RequestForReservationStatus.WAITING_FOR_RESPONSE, response.getStatus());

    }

    @Test
    public void testCreateRequestForReservationUserDoesntExist() {
        String userId = "e49fcaa5-d45b-4556-9d91-13e58187fea6";
        when(restService.getUserById(any(UUID.class))).thenReturn(null);

        String lodgeId = "b86553e1-2552-41cb-9e40-7ef87c424850";
        String lodgeOwnerId = "e49fcab5-d45b-4556-9d91-14e58177fea6";
        mockLodge(lodgeId, lodgeOwnerId);

        mockLodgeAvailabilityPeriods(lodgeId);

        RequestForReservationCreateRequest request = RequestForReservationCreateRequest.builder()
                .lodgeId(UUID.fromString(lodgeId))
                .dateFrom(LocalDateTime.parse("2024-05-02 20:10:21.2632212", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSSS")))
                .dateTo(LocalDateTime.parse("2024-05-04 20:10:21.2632212", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSSS")))
                .numberOfGuests(2)
                .build();

        assertThrows(NotFoundException.class, () -> requestForReservationService.create(request));
    }

    @Test
    public void testCreateRequestForReservationNotLoggedInAsGuest() {
        String userId = "e49fcaa5-d45b-4556-9d91-13e58187fea6";
        UserDto mockUserDTO = UserDto.builder()
                .id(UUID.fromString(userId))
                .role("HOST")
                .build();

        when(restService.getUserById(any(UUID.class))).thenReturn(mockUserDTO);

        String lodgeId = "b86553e1-2552-41cb-9e40-7ef87c424850";
        String lodgeOwnerId = "e49fcab5-d45b-4556-9d91-14e58177fea6";
        mockLodge(lodgeId, lodgeOwnerId);

        mockLodgeAvailabilityPeriods(lodgeId);

        RequestForReservationCreateRequest request = RequestForReservationCreateRequest.builder()
                .lodgeId(UUID.fromString(lodgeId))
                .dateFrom(LocalDateTime.parse("2024-05-02 20:10:21.2632212", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSSS")))
                .dateTo(LocalDateTime.parse("2024-05-04 20:10:21.2632212", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSSS")))
                .numberOfGuests(2)
                .build();

        assertThrows(ForbiddenException.class, () -> requestForReservationService.create(request));
    }

    @Test
    public void testCreateRequestForReservationLodgeDoesntExist() {
        String userId = "e49fcaa5-d45b-4556-9d91-13e58187fea6";
        mockGuest(userId);

        String lodgeId = "b86553e1-2552-41cb-9e40-7ef87c424850";
        String lodgeOwnerId = "e49fcab5-d45b-4556-9d91-14e58177fea6";
        when(restService.getLodgeById(any(UUID.class))).thenReturn(null);

        mockLodgeAvailabilityPeriods(lodgeId);

        RequestForReservationCreateRequest request = RequestForReservationCreateRequest.builder()
                .lodgeId(UUID.fromString(lodgeId))
                .dateFrom(LocalDateTime.parse("2024-05-02 20:10:21.2632212", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSSS")))
                .dateTo(LocalDateTime.parse("2024-05-04 20:10:21.2632212", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSSS")))
                .numberOfGuests(2)
                .build();

        assertThrows(NotFoundException.class, () -> requestForReservationService.create(request));
    }

    @Test
    public void testCreateRequestForReservationLodgeAvailabilityPeriodsDoesntExist() {
        String userId = "e49fcaa5-d45b-4556-9d91-13e58187fea6";
        mockGuest(userId);

        String lodgeId = "b86553e1-2552-41cb-9e40-7ef87c424850";
        String lodgeOwnerId = "e49fcab5-d45b-4556-9d91-14e58177fea6";
        mockLodge(lodgeId, lodgeOwnerId);

        when(restService.getLodgeAvailabilityPeriods(any(UUID.class))).thenReturn(null);

        RequestForReservationCreateRequest request = RequestForReservationCreateRequest.builder()
                .lodgeId(UUID.fromString(lodgeId))
                .dateFrom(LocalDateTime.parse("2024-05-02 20:10:21.2632212", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSSS")))
                .dateTo(LocalDateTime.parse("2024-05-04 20:10:21.2632212", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSSS")))
                .numberOfGuests(2)
                .build();

        assertThrows(NotFoundException.class, () -> requestForReservationService.create(request));
    }

    // tests for Check methods

    @Test
    public void testCreateRequestForReservationNumberOfGuestsIsNotInRange() {
        String userId = "e49fcaa5-d45b-4556-9d91-13e58187fea6";
        mockGuest(userId);

        String lodgeId = "b86553e1-2552-41cb-9e40-7ef87c424850";
        String lodgeOwnerId = "e49fcab5-d45b-4556-9d91-14e58177fea6";
        mockLodge(lodgeId, lodgeOwnerId);

        mockLodgeAvailabilityPeriods(lodgeId);

        RequestForReservationCreateRequest request = RequestForReservationCreateRequest.builder()
                .lodgeId(UUID.fromString(lodgeId))
                .dateFrom(LocalDateTime.parse("2024-05-02 20:10:21.2632212", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSSS")))
                .dateTo(LocalDateTime.parse("2024-05-04 20:10:21.2632212", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSSS")))
                .numberOfGuests(4)
                .build();

        assertThrows(BadRequestException.class, () -> requestForReservationService.create(request));
    }

    @Test
    public void testCreateRequestForReservationDateFromIsNotEarlierThanDateTo() {
        String userId = "e49fcaa5-d45b-4556-9d91-13e58187fea6";
        mockGuest(userId);

        String lodgeId = "b86553e1-2552-41cb-9e40-7ef87c424850";
        String lodgeOwnerId = "e49fcab5-d45b-4556-9d91-14e58177fea6";
        mockLodge(lodgeId, lodgeOwnerId);

        mockLodgeAvailabilityPeriods(lodgeId);

        RequestForReservationCreateRequest request = RequestForReservationCreateRequest.builder()
                .lodgeId(UUID.fromString(lodgeId))
                .dateFrom(LocalDateTime.parse("2024-05-04 20:10:21.2632212", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSSS")))
                .dateTo(LocalDateTime.parse("2024-05-02 20:10:21.2632212", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSSS")))
                .numberOfGuests(2)
                .build();

        assertThrows(BadRequestException.class, () -> requestForReservationService.create(request));
    }

    @Test
    public void testCreateRequestForReservationNoLodgeAvailabilityPeriodForSelectedDateRange() {
        String userId = "e49fcaa5-d45b-4556-9d91-13e58187fea6";
        mockGuest(userId);

        String lodgeId = "b86553e1-2552-41cb-9e40-7ef87c424850";
        String lodgeOwnerId = "e49fcab5-d45b-4556-9d91-14e58177fea6";
        mockLodge(lodgeId, lodgeOwnerId);

        mockLodgeAvailabilityPeriods(lodgeId);

        RequestForReservationCreateRequest request = RequestForReservationCreateRequest.builder()
                .lodgeId(UUID.fromString(lodgeId))
                .dateFrom(LocalDateTime.parse("2024-06-02 20:10:21.2632212", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSSS")))
                .dateTo(LocalDateTime.parse("2024-06-04 20:10:21.2632212", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSSS")))
                .numberOfGuests(2)
                .build();

        assertThrows(BadRequestException.class, () -> requestForReservationService.create(request));
    }

    @Test
    public void testCreateRequestForReservationApprovedRequestExists() {
        String userId = "e49fcaa5-d45b-4556-9d91-13e58187fea6";
        mockGuest(userId);

        String lodgeId = "b86553e1-2552-41cb-9e40-7ef87c424850";
        String lodgeOwnerId = "e49fcab5-d45b-4556-9d91-14e58177fea6";
        mockLodge(lodgeId, lodgeOwnerId);

        mockLodgeAvailabilityPeriods(lodgeId);

        RequestForReservationCreateRequest request = RequestForReservationCreateRequest.builder()
                .lodgeId(UUID.fromString(lodgeId))
                .dateFrom(LocalDateTime.parse("2024-05-02 20:10:21.2632212", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSSS")))
                .dateTo(LocalDateTime.parse("2024-05-14 20:10:21.2632212", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSSS")))
                .numberOfGuests(2)
                .build();

        assertThrows(BadRequestException.class, () -> requestForReservationService.create(request));
    }

    @Test
    public void testCreateRequestForReservationCalculatePricePerGuestSuccessful() {
        String userId = "e49fcaa5-d45b-4556-9d91-13e58187fea6";
        mockGuest(userId);

        String lodgeId = "b86553e1-2552-41cb-9e40-7ef87c424850";
        String lodgeOwnerId = "e49fcab5-d45b-4556-9d91-14e58177fea6";
        mockLodge(lodgeId, lodgeOwnerId);

        mockLodgeAvailabilityPeriods2(lodgeId);

        RequestForReservationCreateRequest request = RequestForReservationCreateRequest.builder()
                .lodgeId(UUID.fromString(lodgeId))
                .dateFrom(LocalDateTime.parse("2024-05-02 20:10:21.2632212", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSSS")))
                .dateTo(LocalDateTime.parse("2024-05-05 20:10:21.2632212", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSSS")))
                .numberOfGuests(3)
                .build();

        RequestForReservationDto response = requestForReservationService.create(request);

        assertNotNull(response);
        assertEquals(request.getLodgeId(), response.getLodgeId());
        assertEquals(UUID.fromString(userId), response.getGuestId());
        assertEquals(UUID.fromString(lodgeOwnerId), response.getOwnerId());
        assertEquals(540.0, response.getPrice());
        assertEquals(request.getDateFrom(), response.getDateFrom());
        assertEquals(request.getDateTo(), response.getDateTo());
        assertEquals(request.getNumberOfGuests(), response.getNumberOfGuests());
        assertEquals(RequestForReservationStatus.WAITING_FOR_RESPONSE, response.getStatus());
    }

    @Test
    public void testCreateRequestForReservationCalculatePricePerLodgeSuccessful() {
        String userId = "e49fcaa5-d45b-4556-9d91-13e58187fea6";
        mockGuest(userId);

        String lodgeId = "b86553e1-2552-41cb-9e40-7ef87c424850";
        String lodgeOwnerId = "e49fcab5-d45b-4556-9d91-14e58177fea6";
        mockLodge(lodgeId, lodgeOwnerId);

        mockLodgeAvailabilityPeriods(lodgeId);

        RequestForReservationCreateRequest request = RequestForReservationCreateRequest.builder()
                .lodgeId(UUID.fromString(lodgeId))
                .dateFrom(LocalDateTime.parse("2024-05-02 20:10:21.2632212", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSSS")))
                .dateTo(LocalDateTime.parse("2024-05-05 20:10:21.2632212", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSSS")))
                .numberOfGuests(3)
                .build();

        RequestForReservationDto response = requestForReservationService.create(request);

        assertNotNull(response);
        assertEquals(request.getLodgeId(), response.getLodgeId());
        assertEquals(UUID.fromString(userId), response.getGuestId());
        assertEquals(UUID.fromString(lodgeOwnerId), response.getOwnerId());
        assertEquals(180.0, response.getPrice());
        assertEquals(request.getDateFrom(), response.getDateFrom());
        assertEquals(request.getDateTo(), response.getDateTo());
        assertEquals(request.getNumberOfGuests(), response.getNumberOfGuests());
        assertEquals(RequestForReservationStatus.WAITING_FOR_RESPONSE, response.getStatus());
    }

    private void mockGuest(String userId) {
        UserDto mockUserDTO = UserDto.builder()
                .id(UUID.fromString(userId))
                .role("GUEST")
                .build();

        when(restService.getUserById(any(UUID.class))).thenReturn(mockUserDTO);
    }

    private void mockLodge(String lodgeId, String lodgeOwnerId) {
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

    private void mockLodgeAvailabilityPeriods(String lodgeId) {
        String lodgeAvailabilityPeriod1Id = "fb809d54-332d-4811-8d93-d3ddf2f345a2";
        LodgeAvailabilityPeriodDto mockLodgeAvailabilityPeriodDTO1 = LodgeAvailabilityPeriodDto.builder()
                .id(UUID.fromString(lodgeAvailabilityPeriod1Id))
                .lodgeId(UUID.fromString(lodgeId))
                .dateFrom(LocalDateTime.parse("2024-05-01 20:10:21.2632212", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSSS")))
                .dateTo(LocalDateTime.parse("2024-05-29 20:10:21.2632212", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSSS")))
                .priceType("PER_LODGE")
                .price(60)
                .build();

        List<LodgeAvailabilityPeriodDto> mockAvailabilityPeriods = new ArrayList<>();
        mockAvailabilityPeriods.add(mockLodgeAvailabilityPeriodDTO1);

        when(restService.getLodgeAvailabilityPeriods(any(UUID.class))).thenReturn(mockAvailabilityPeriods);
    }

    private void mockLodgeAvailabilityPeriods2(String lodgeId) {
        String lodgeAvailabilityPeriod1Id = "fb809d54-332d-4811-8d93-d3ddf2f345a2";
        LodgeAvailabilityPeriodDto mockLodgeAvailabilityPeriodDTO1 = LodgeAvailabilityPeriodDto.builder()
                .id(UUID.fromString(lodgeAvailabilityPeriod1Id))
                .lodgeId(UUID.fromString(lodgeId))
                .dateFrom(LocalDateTime.parse("2024-05-01 20:10:21.2632212", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSSS")))
                .dateTo(LocalDateTime.parse("2024-05-29 20:10:21.2632212", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSSS")))
                .priceType("PER_GUEST")
                .price(60)
                .build();

        List<LodgeAvailabilityPeriodDto> mockAvailabilityPeriods = new ArrayList<>();
        mockAvailabilityPeriods.add(mockLodgeAvailabilityPeriodDTO1);

        when(restService.getLodgeAvailabilityPeriods(any(UUID.class))).thenReturn(mockAvailabilityPeriods);
    }

}
