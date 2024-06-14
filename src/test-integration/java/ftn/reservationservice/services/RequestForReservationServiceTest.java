package ftn.reservationservice.services;

import ftn.reservationservice.AuthPostgresIntegrationTest;
import ftn.reservationservice.domain.dtos.*;
import ftn.reservationservice.domain.entities.RequestForReservationStatus;
import ftn.reservationservice.exception.exceptions.BadRequestException;
import ftn.reservationservice.exception.exceptions.ForbiddenException;
import ftn.reservationservice.exception.exceptions.NotFoundException;
import ftn.reservationservice.repositories.RequestForReservationRepository;
import ftn.reservationservice.repositories.ReservationRepository;
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

    @Autowired
    private RequestForReservationRepository requestForReservationRepository;

    @Autowired
    private ReservationRepository reservationRepository;

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
        mockLodgeAutomaticApproval(lodgeId, lodgeOwnerId);

        mockLodgeAvailabilityPeriods(lodgeId);

        RequestForReservationCreateRequest request = RequestForReservationCreateRequest.builder()
                .lodgeId(UUID.fromString(lodgeId))
                .dateFrom(LocalDateTime.parse("2024-05-02 00:00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .dateTo(LocalDateTime.parse("2024-05-04 00:00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
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
        assertEquals(RequestForReservationStatus.APPROVED, response.getStatus());

    }

    @Test
    public void testCreateRequestForReservationUserDoesntExist() {
        String userId = "e49fcaa5-d45b-4556-9d91-13e58187fea6";
        when(restService.getUserById(any(UUID.class))).thenReturn(null);

        String lodgeId = "b86553e1-2552-41cb-9e40-7ef87c424850";
        String lodgeOwnerId = "e49fcab5-d45b-4556-9d91-14e58177fea6";
        mockLodgeAutomaticApproval(lodgeId, lodgeOwnerId);

        mockLodgeAvailabilityPeriods(lodgeId);

        RequestForReservationCreateRequest request = RequestForReservationCreateRequest.builder()
                .lodgeId(UUID.fromString(lodgeId))
                .dateFrom(LocalDateTime.parse("2024-05-02 00:00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .dateTo(LocalDateTime.parse("2024-05-04 00:00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
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
        mockLodgeAutomaticApproval(lodgeId, lodgeOwnerId);

        mockLodgeAvailabilityPeriods(lodgeId);

        RequestForReservationCreateRequest request = RequestForReservationCreateRequest.builder()
                .lodgeId(UUID.fromString(lodgeId))
                .dateFrom(LocalDateTime.parse("2024-05-02 00:00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .dateTo(LocalDateTime.parse("2024-05-04 00:00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
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
                .dateFrom(LocalDateTime.parse("2024-05-02 00:00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .dateTo(LocalDateTime.parse("2024-05-04 00:00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
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
        mockLodgeAutomaticApproval(lodgeId, lodgeOwnerId);

        when(restService.getLodgeAvailabilityPeriods(any(UUID.class))).thenReturn(null);

        RequestForReservationCreateRequest request = RequestForReservationCreateRequest.builder()
                .lodgeId(UUID.fromString(lodgeId))
                .dateFrom(LocalDateTime.parse("2024-05-02 00:00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .dateTo(LocalDateTime.parse("2024-05-04 00:00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
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
        mockLodgeAutomaticApproval(lodgeId, lodgeOwnerId);

        mockLodgeAvailabilityPeriods(lodgeId);

        RequestForReservationCreateRequest request = RequestForReservationCreateRequest.builder()
                .lodgeId(UUID.fromString(lodgeId))
                .dateFrom(LocalDateTime.parse("2024-05-02 00:00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .dateTo(LocalDateTime.parse("2024-05-04 00:00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
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
        mockLodgeAutomaticApproval(lodgeId, lodgeOwnerId);

        mockLodgeAvailabilityPeriods(lodgeId);

        RequestForReservationCreateRequest request = RequestForReservationCreateRequest.builder()
                .lodgeId(UUID.fromString(lodgeId))
                .dateFrom(LocalDateTime.parse("2024-05-04 00:00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .dateTo(LocalDateTime.parse("2024-05-02 00:00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
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
        mockLodgeAutomaticApproval(lodgeId, lodgeOwnerId);

        mockLodgeAvailabilityPeriods(lodgeId);

        RequestForReservationCreateRequest request = RequestForReservationCreateRequest.builder()
                .lodgeId(UUID.fromString(lodgeId))
                .dateFrom(LocalDateTime.parse("2024-06-02 00:00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .dateTo(LocalDateTime.parse("2024-06-04 00:00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
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
        mockLodgeAutomaticApproval(lodgeId, lodgeOwnerId);

        mockLodgeAvailabilityPeriods(lodgeId);

        RequestForReservationCreateRequest request = RequestForReservationCreateRequest.builder()
                .lodgeId(UUID.fromString(lodgeId))
                .dateFrom(LocalDateTime.parse("2024-05-02 00:00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .dateTo(LocalDateTime.parse("2024-05-14 00:00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
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
        mockLodgeAutomaticApproval(lodgeId, lodgeOwnerId);

        mockLodgeAvailabilityPeriods2(lodgeId);

        RequestForReservationCreateRequest request = RequestForReservationCreateRequest.builder()
                .lodgeId(UUID.fromString(lodgeId))
                .dateFrom(LocalDateTime.parse("2024-05-02 00:00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .dateTo(LocalDateTime.parse("2024-05-05 00:00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
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
        assertEquals(RequestForReservationStatus.APPROVED, response.getStatus());
    }

    @Test
    public void testCreateRequestForReservationCalculatePricePerLodgeSuccessful() {
        String userId = "e49fcaa5-d45b-4556-9d91-13e58187fea6";
        mockGuest(userId);

        String lodgeId = "b86553e1-2552-41cb-9e40-7ef87c424850";
        String lodgeOwnerId = "e49fcab5-d45b-4556-9d91-14e58177fea6";
        mockLodgeAutomaticApproval(lodgeId, lodgeOwnerId);

        mockLodgeAvailabilityPeriods(lodgeId);

        RequestForReservationCreateRequest request = RequestForReservationCreateRequest.builder()
                .lodgeId(UUID.fromString(lodgeId))
                .dateFrom(LocalDateTime.parse("2024-05-02 00:00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .dateTo(LocalDateTime.parse("2024-05-05 00:00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
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
        assertEquals(RequestForReservationStatus.APPROVED, response.getStatus());
    }

    // DELETE tests

    @Test
    public void testDeleteRequestForReservationSuccessful() {
        String userId = "e49fcaa5-d45b-4556-9d91-13e58187fea6";
        mockGuest(userId);

        UUID requestForReservationId = UUID.fromString("b86553e1-2552-41cb-9e40-7eeeee424891");

        requestForReservationService.delete(requestForReservationId);

        assertFalse(requestForReservationRepository.findById(requestForReservationId).isPresent());
    }

    @Test
    public void testDeleteRequestForReservationUserDoesntExist() {
        String userId = "e49fcaa5-d45b-4556-9d91-13e58187fea6";
        when(restService.getUserById(any(UUID.class))).thenReturn(null);

        UUID requestForReservationId = UUID.fromString("b86553e1-2552-41cb-9e40-7eeeee424891");

        assertThrows(NotFoundException.class, () -> requestForReservationService.delete(requestForReservationId));

        assertTrue(requestForReservationRepository.findById(requestForReservationId).isPresent());
    }

    @Test
    public void testDeleteRequestForReservationNotLoggedInAsGuest() {
        String userId = "e49fcaa5-d45b-4556-9d91-13e58187fea6";
        UserDto mockUserDTO = UserDto.builder()
                .id(UUID.fromString(userId))
                .role("HOST")
                .build();
        when(restService.getUserById(any(UUID.class))).thenReturn(mockUserDTO);

        UUID requestForReservationId = UUID.fromString("b86553e1-2552-41cb-9e40-7eeeee424891");
        assertThrows(ForbiddenException.class, () -> requestForReservationService.delete(requestForReservationId));

        assertTrue(requestForReservationRepository.findById(requestForReservationId).isPresent());
    }

    @Test
    public void testDeleteRequestForReservationThatDoesntExist() {
        String userId = "e49fcaa5-d45b-4556-9d91-13e58187fea6";
        mockGuest(userId);

        UUID requestForReservationId = UUID.fromString("b86553e1-2552-41cb-9e40-7eeeee424891");
        UUID wrongRequestForReservationId = UUID.fromString("b86553e1-2552-41cb-9e40-7eeeee424892");
        assertThrows(NotFoundException.class, () -> requestForReservationService.delete(wrongRequestForReservationId));

        assertTrue(requestForReservationRepository.findById(requestForReservationId).isPresent());
    }

    @Test
    public void testDeleteRequestForReservationGuestDidntMadeRequestForReservation() {
        String userId = "e49fcaa5-d45b-4556-9d91-13e58187fea6";
        mockGuest(userId);

        UUID requestForReservationId = UUID.fromString("b86553e1-2552-41cb-9e40-7eeeee424894");
        assertThrows(ForbiddenException.class, () -> requestForReservationService.delete(requestForReservationId));

        assertTrue(requestForReservationRepository.findById(requestForReservationId).isPresent());
    }

    @Test
    public void testDeleteRequestForReservationStatusDoesntEnablesDeletion() {
        String userId = "e49fcaa5-d45b-4556-9d91-13e58187fea6";
        mockGuest(userId);

        UUID requestForReservationId = UUID.fromString("b86553e1-2552-41cb-9e40-7eeeee424850");
        assertThrows(BadRequestException.class, () -> requestForReservationService.delete(requestForReservationId));

        assertTrue(requestForReservationRepository.findById(requestForReservationId).isPresent());
    }

    // Update

    @Test
    public void testUpdateRequestForReservationApprovedSuccessful() {
        String lodgeOwnerId = "e49fcab5-d45b-4556-9d91-14e58177fea6";
        mockOwner(lodgeOwnerId);

        String lodgeId = "b86553e1-2552-41cb-9e40-7ef87c424850";
        mockLodgeManualApproval(lodgeId, lodgeOwnerId);
        mockLodgeAvailabilityPeriods(lodgeId);

        String guestId = "e49fcaa5-d45b-4556-9d91-13e58187fea6";

        String requestForReservationId = "b86553e1-2552-41cb-9e40-7eeeee424891";

        RequestForReservationStatusUpdateRequest updateRequest = RequestForReservationStatusUpdateRequest.builder()
                .status(RequestForReservationStatus.APPROVED)
                .build();

        RequestForReservationDto response = requestForReservationService.update(UUID.fromString(requestForReservationId),
                updateRequest);

        assertNotNull(response);
        assertEquals(UUID.fromString(lodgeId), response.getLodgeId());
        assertEquals(UUID.fromString(guestId), response.getGuestId());
        assertEquals(UUID.fromString(lodgeOwnerId), response.getOwnerId());
        assertEquals(99.99, response.getPrice());
        assertEquals(RequestForReservationStatus.APPROVED, response.getStatus());

        assertEquals(1, reservationRepository.findAll().size());

    }

    @Test
    public void testUpdateRequestForReservationDeniedSuccessful() {
        String lodgeOwnerId = "e49fcab5-d45b-4556-9d91-14e58177fea6";
        mockOwner(lodgeOwnerId);

        String lodgeId = "b86553e1-2552-41cb-9e40-7ef87c424850";
        mockLodgeManualApproval(lodgeId, lodgeOwnerId);
        mockLodgeAvailabilityPeriods(lodgeId);

        String guestId = "e49fcaa5-d45b-4556-9d91-13e58187fea6";

        String requestForReservationId = "b86553e1-2552-41cb-9e40-7eeeee424891";

        RequestForReservationStatusUpdateRequest updateRequest = RequestForReservationStatusUpdateRequest.builder()
                .status(RequestForReservationStatus.DENIED)
                .build();

        RequestForReservationDto response = requestForReservationService.update(UUID.fromString(requestForReservationId),
                updateRequest);

        assertNotNull(response);
        assertEquals(UUID.fromString(lodgeId), response.getLodgeId());
        assertEquals(UUID.fromString(guestId), response.getGuestId());
        assertEquals(UUID.fromString(lodgeOwnerId), response.getOwnerId());
        assertEquals(99.99, response.getPrice());
        assertEquals(RequestForReservationStatus.DENIED, response.getStatus());

        assertEquals(0, reservationRepository.findAll().size());

    }

    @Test
    public void testUpdateRequestForReservationRequestDoesntExist() {
        String lodgeOwnerId = "e49fcab5-d45b-4556-9d91-14e58177fea6";
        mockOwner(lodgeOwnerId);

        String lodgeId = "b86553e1-2552-41cb-9e40-7ef87c424850";
        mockLodgeManualApproval(lodgeId, lodgeOwnerId);

        String requestForReservationId = "b86553e1-2552-41cb-9e40-7eeeee424892";

        RequestForReservationStatusUpdateRequest updateRequest = RequestForReservationStatusUpdateRequest.builder()
                .status(RequestForReservationStatus.DENIED)
                .build();

        assertThrows(NotFoundException.class, () -> requestForReservationService.update(UUID.fromString(requestForReservationId),
                updateRequest));

        assertEquals(0, reservationRepository.findAll().size());
    }

    @Test
    public void testUpdateRequestForReservationStatusIsNotAppropriate() {
        String lodgeOwnerId = "e49fcab5-d45b-4556-9d91-14e58177fea6";
        mockOwner(lodgeOwnerId);

        String lodgeId = "b86553e1-2552-41cb-9e40-7ef87c424850";
        mockLodgeManualApproval(lodgeId, lodgeOwnerId);

        String requestForReservationId = "b86553e1-2552-41cb-9e40-7eeeee424823";

        RequestForReservationStatusUpdateRequest updateRequest = RequestForReservationStatusUpdateRequest.builder()
                .status(RequestForReservationStatus.APPROVED)
                .build();

        assertThrows(BadRequestException.class, () -> requestForReservationService.update(UUID.fromString(requestForReservationId),
                updateRequest));

        assertEquals(0, reservationRepository.findAll().size());
    }

    @Test
    public void testUpdateRequestForReservationUserDoesntExist() {
        String lodgeOwnerId = "e49fcab5-d45b-4556-9d91-14e58177fea6";
        when(restService.getUserById(any(UUID.class))).thenReturn(null);

        String lodgeId = "b86553e1-2552-41cb-9e40-7ef87c424850";
        mockLodgeManualApproval(lodgeId, lodgeOwnerId);

        String requestForReservationId = "b86553e1-2552-41cb-9e40-7eeeee424892";

        RequestForReservationStatusUpdateRequest updateRequest = RequestForReservationStatusUpdateRequest.builder()
                .status(RequestForReservationStatus.APPROVED)
                .build();

        assertThrows(NotFoundException.class, () -> requestForReservationService.update(UUID.fromString(requestForReservationId),
                updateRequest));

        assertEquals(0, reservationRepository.findAll().size());
    }

    @Test
    public void testUpdateRequestForReservationLodgeDoesntExist() {
        String lodgeOwnerId = "e49fcab5-d45b-4556-9d91-14e58177fea6";
        mockOwner(lodgeOwnerId);

        String lodgeId = "b86553e1-2552-41cb-9e40-7ef87c424850";
        when(restService.getLodgeById(any(UUID.class))).thenReturn(null);

        String requestForReservationId = "b86553e1-2552-41cb-9e40-7eeeee424892";

        RequestForReservationStatusUpdateRequest updateRequest = RequestForReservationStatusUpdateRequest.builder()
                .status(RequestForReservationStatus.APPROVED)
                .build();

        assertThrows(NotFoundException.class, () -> requestForReservationService.update(UUID.fromString(requestForReservationId),
                updateRequest));

        assertEquals(0, reservationRepository.findAll().size());
    }

    @Test
    public void testUpdateRequestForReservationLoggedInUserIsNotLodgeOwner() {
        String lodgeOwnerId = "e49fcab5-d45b-4556-9d91-14e58177fea6";
        mockOwner(lodgeOwnerId);

        String lodgeId = "b86553e1-2552-41cb-9e40-7ef87c424850";
        String fakeLodgeOwner = "e49fcab5-d45b-4556-9d91-14e58177aaa1";
        mockLodgeManualApproval(lodgeId, fakeLodgeOwner);

        String requestForReservationId = "b86553e1-2552-41cb-9e40-7eeeee424833";

        RequestForReservationStatusUpdateRequest updateRequest = RequestForReservationStatusUpdateRequest.builder()
                .status(RequestForReservationStatus.APPROVED)
                .build();

        assertThrows(BadRequestException.class, () -> requestForReservationService.update(UUID.fromString(requestForReservationId),
                updateRequest));

        assertEquals(0, reservationRepository.findAll().size());
    }

    @Test
    public void testUpdateRequestForReservationManualApprovalNotSelected() {
        String lodgeOwnerId = "e49fcab5-d45b-4556-9d91-14e58177fea6";
        mockOwner(lodgeOwnerId);

        String lodgeId = "b86553e1-2552-41cb-9e40-7ef87c424850";
        mockLodgeAutomaticApproval(lodgeId, lodgeOwnerId);

        String requestForReservationId = "b86553e1-2552-41cb-9e40-7eeeee424891";

        RequestForReservationStatusUpdateRequest updateRequest = RequestForReservationStatusUpdateRequest.builder()
                .status(RequestForReservationStatus.APPROVED)
                .build();

        assertThrows(BadRequestException.class, () -> requestForReservationService.update(UUID.fromString(requestForReservationId),
                updateRequest));

        assertEquals(0, reservationRepository.findAll().size());
    }

    @Test
    public void testUpdateRequestForReservationStatusAfterUpdateNotValid() {
        String lodgeOwnerId = "e49fcab5-d45b-4556-9d91-14e58177fea6";
        mockOwner(lodgeOwnerId);

        String lodgeId = "b86553e1-2552-41cb-9e40-7ef87c424850";
        mockLodgeManualApproval(lodgeId, lodgeOwnerId);

        String requestForReservationId = "b86553e1-2552-41cb-9e40-7eeeee424891";

        RequestForReservationStatusUpdateRequest updateRequest = RequestForReservationStatusUpdateRequest.builder()
                .status(RequestForReservationStatus.WAITING_FOR_RESPONSE)
                .build();

        assertThrows(BadRequestException.class, () -> requestForReservationService.update(UUID.fromString(requestForReservationId),
                updateRequest));

        assertEquals(0, reservationRepository.findAll().size());
    }

    // Get

    @Test
    public void testGetHostReservationRequestsSuccess() {
        String lodgeOwnerId = "e49fcab5-d45b-4556-9d91-14e58177fea6";
        mockOwner(lodgeOwnerId);

        List<RequestForReservationDto> response = requestForReservationService.getHostReservationRequests();

        assertNotNull(response);
        assertEquals(4, response.size());

    }

    @Test
    public void testGetHostReservationRequestsOwnerNotFound() {
        String lodgeOwnerId = "e49fcab5-d45b-4556-9d91-14e58177fea6";
        when(restService.getUserById(any(UUID.class))).thenReturn(null);

        assertThrows(NotFoundException.class, () -> requestForReservationService.getHostReservationRequests());
    }

    @Test
    public void testGetGuestReservationRequestsSuccess() {
        String guestId = "e49fcaa5-d45b-4556-9d91-13e58187fea6";
        mockGuest(guestId);

        List<RequestForReservationDto> response = requestForReservationService.getGuestReservationRequests();

        assertNotNull(response);
        assertEquals(4, response.size());

    }

    @Test
    public void testGetGuestReservationRequestsGuestNotFound() {
        String lodgeOwnerId = "e49fcab5-d45b-4556-9d91-14e58177fea6";
        when(restService.getUserById(any(UUID.class))).thenReturn(null);

        assertThrows(NotFoundException.class, () -> requestForReservationService.getGuestReservationRequests());
    }

    @Test
    public void testGetReservationRequestByIdHostSuccess() {
        String lodgeOwnerId = "e49fcab5-d45b-4556-9d91-14e58177fea6";
        mockOwner(lodgeOwnerId);

        String requestForReservationId = "b86553e1-2552-41cb-9e40-7eeeee424891";

        RequestForReservationDto response = requestForReservationService.getReservationRequestByIdHost(UUID.fromString(requestForReservationId));

        assertNotNull(response);
        assertEquals(UUID.fromString("b86553e1-2552-41cb-9e40-7ef87c424850"), response.getLodgeId());
        assertEquals(UUID.fromString("e49fcaa5-d45b-4556-9d91-13e58187fea6"), response.getGuestId());
        assertEquals(UUID.fromString(lodgeOwnerId), response.getOwnerId());
        assertEquals(99.99, response.getPrice());
        assertEquals(LocalDateTime.parse("2024-05-19 00:00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")), response.getDateFrom());
        assertEquals(LocalDateTime.parse("2024-05-23 00:00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")), response.getDateTo());
        assertEquals(2, response.getNumberOfGuests());
        assertEquals(RequestForReservationStatus.WAITING_FOR_RESPONSE, response.getStatus());

    }

    @Test
    public void testGetReservationRequestByIdHostOwnerNotFound() {
        String lodgeOwnerId = "e49fcab5-d45b-4556-9d91-14e58177fea6";
        when(restService.getUserById(any(UUID.class))).thenReturn(null);

        String requestForReservationId = "b86553e1-2552-41cb-9e40-7eeeee424891";

        assertThrows(NotFoundException.class, () -> requestForReservationService.getReservationRequestByIdHost(UUID.fromString(requestForReservationId)));
    }

    @Test
    public void testGetReservationRequestByIdHostRequestNotFound() {
        String lodgeOwnerId = "e49fcab5-d45b-4556-9d91-14e58177fea6";
        mockOwner(lodgeOwnerId);

        String requestForReservationId = "b86543e1-2552-41cb-9e40-7eeeee424891";

        assertThrows(NotFoundException.class, () -> requestForReservationService.getReservationRequestByIdHost(UUID.fromString(requestForReservationId)));
    }

    @Test
    public void testGetReservationRequestByIdHostThatIsNotOwner() {
        String lodgeOwnerId = "e49fcab5-d45b-4556-9d91-14e58177fea6";
        mockOwner(lodgeOwnerId);

        String requestForReservationId = "b86553e1-2552-41cb-9e40-7eeeee424833";

        assertThrows(ForbiddenException.class, () -> requestForReservationService.getReservationRequestByIdHost(UUID.fromString(requestForReservationId)));
    }

    @Test
    public void testGetReservationRequestByIdGuestSuccess() {
        String guestId = "e49fcaa5-d45b-4556-9d91-13e58187fea6";
        mockGuest(guestId);

        String lodgeOwnerId = "e49fcab5-d45b-4556-9d91-14e58177fea6";
        String requestForReservationId = "b86553e1-2552-41cb-9e40-7eeeee424891";

        RequestForReservationDto response = requestForReservationService.getReservationRequestByIdGuest(UUID.fromString(requestForReservationId));

        assertNotNull(response);
        assertEquals(UUID.fromString("b86553e1-2552-41cb-9e40-7ef87c424850"), response.getLodgeId());
        assertEquals(UUID.fromString("e49fcaa5-d45b-4556-9d91-13e58187fea6"), response.getGuestId());
        assertEquals(UUID.fromString(lodgeOwnerId), response.getOwnerId());
        assertEquals(99.99, response.getPrice());
        assertEquals(LocalDateTime.parse("2024-05-19 00:00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")), response.getDateFrom());
        assertEquals(LocalDateTime.parse("2024-05-23 00:00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")), response.getDateTo());
        assertEquals(2, response.getNumberOfGuests());
        assertEquals(RequestForReservationStatus.WAITING_FOR_RESPONSE, response.getStatus());

    }

    @Test
    public void testGetReservationRequestByIdGuestNotFound() {
        when(restService.getUserById(any(UUID.class))).thenReturn(null);

        String requestForReservationId = "b86553e1-2552-41cb-9e40-7eeeee424891";

        assertThrows(NotFoundException.class, () -> requestForReservationService.getReservationRequestByIdGuest(UUID.fromString(requestForReservationId)));
    }

    @Test
    public void testGetReservationRequestByIdGuestRequestNotFound() {
        String guestId = "e49fcaa5-d45b-4556-9d91-13e58187fea6";
        mockGuest(guestId);

        String requestForReservationId = "b86543e1-2552-41cb-9e40-7eeeee424891";

        assertThrows(NotFoundException.class, () -> requestForReservationService.getReservationRequestByIdGuest(UUID.fromString(requestForReservationId)));
    }

    @Test
    public void testGetReservationRequestByIdGuestThatDidntMadeRequest() {
        String guestId = "e49fcaa5-d45b-4556-9d91-13e58187fea6";
        mockGuest(guestId);

        String requestForReservationId = "b86553e1-2552-41cb-9e40-7eeeee424894";

        assertThrows(ForbiddenException.class, () -> requestForReservationService.getReservationRequestByIdGuest(UUID.fromString(requestForReservationId)));
    }

    @Test
    public void testCheckIfRequestForReservationExistsSuccess() {
        UUID lodgeId = UUID.fromString("b86553e1-2552-41cb-9e40-7ef87c424850");
        LocalDateTime dateFrom = LocalDateTime.parse("2024-05-12T00:00:00");
        LocalDateTime dateTo = LocalDateTime.parse("2024-05-18T00:00:00");

        BoolCheckResponseDto response = requestForReservationService.checkIfRequestForReservationExists(lodgeId, dateFrom, dateTo);

        assertNotNull(response);
        assertTrue(response.isValue());

    }

    @Test
    public void testCheckIfRequestForReservationExistsWhenItDoesnt() {
        UUID lodgeId = UUID.fromString("b86553e1-2552-41cb-9e40-7ef87c424850");
        LocalDateTime dateFrom = LocalDateTime.parse("2024-04-12T00:00:00");
        LocalDateTime dateTo = LocalDateTime.parse("2024-04-18T23:59:59");

        BoolCheckResponseDto response = requestForReservationService.checkIfRequestForReservationExists(lodgeId, dateFrom, dateTo);

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

    private void mockLodgeManualApproval(String lodgeId, String lodgeOwnerId) {
        LodgeDto mockLodgeDTO = LodgeDto.builder()
                .id(UUID.fromString(lodgeId))
                .ownerId(UUID.fromString(lodgeOwnerId))
                .name("Vikendica")
                .location("Lokacija1")
                .minimalGuestNumber(1)
                .maximalGuestNumber(3)
                .approvalType("MANUAL")
                .build();

        when(restService.getLodgeById(any(UUID.class))).thenReturn(mockLodgeDTO);
    }

    private void mockLodgeAvailabilityPeriods(String lodgeId) {
        String lodgeAvailabilityPeriod1Id = "fb809d54-332d-4811-8d93-d3ddf2f345a2";
        LodgeAvailabilityPeriodDto mockLodgeAvailabilityPeriodDTO1 = LodgeAvailabilityPeriodDto.builder()
                .id(UUID.fromString(lodgeAvailabilityPeriod1Id))
                .lodgeId(UUID.fromString(lodgeId))
                .dateFrom(LocalDateTime.parse("2024-05-01 00:00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .dateTo(LocalDateTime.parse("2024-05-29 00:00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
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
                .dateFrom(LocalDateTime.parse("2024-05-01 00:00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .dateTo(LocalDateTime.parse("2024-05-29 00:00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .priceType("PER_GUEST")
                .price(60)
                .build();

        List<LodgeAvailabilityPeriodDto> mockAvailabilityPeriods = new ArrayList<>();
        mockAvailabilityPeriods.add(mockLodgeAvailabilityPeriodDTO1);

        when(restService.getLodgeAvailabilityPeriods(any(UUID.class))).thenReturn(mockAvailabilityPeriods);
    }

}
