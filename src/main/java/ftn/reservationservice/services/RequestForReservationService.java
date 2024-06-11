package ftn.reservationservice.services;

import ftn.reservationservice.domain.dtos.*;
import ftn.reservationservice.domain.entities.RequestForReservation;
import ftn.reservationservice.domain.entities.RequestForReservationStatus;
import ftn.reservationservice.domain.entities.Reservation;
import ftn.reservationservice.domain.mappers.RequestForReservationMapper;
import ftn.reservationservice.exception.exceptions.BadRequestException;
import ftn.reservationservice.exception.exceptions.ForbiddenException;
import ftn.reservationservice.exception.exceptions.NotFoundException;
import ftn.reservationservice.repositories.RequestForReservationRepository;
import ftn.reservationservice.utils.AuthUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class RequestForReservationService {

    private final RestService restService;

    private final RequestForReservationRepository requestForReservationRepository;

    private final ReservationService reservationService;

    public RequestForReservationDto create(RequestForReservationCreateRequest requestForReservationCreateRequest) {
        RequestForReservation request = RequestForReservationMapper.INSTANCE.fromCreateRequest(requestForReservationCreateRequest);
        UserDto guest = getLoggedInUser();
        checkLoggedInUserIsGuest(guest);
        LodgeDto lodge = getLodge(request.getLodgeId());
        List<LodgeAvailabilityPeriodDto> availabilityPeriods = getLodgeAvailabilityPeriods(request.getLodgeId());

        createChecks(request, lodge, availabilityPeriods);

        request.setGuestId(guest.getId());
        request.setOwnerId(lodge.getOwnerId());
        double calculatedPrice = calculatePrice(request, availabilityPeriods);
        request.setPrice(calculatedPrice);

        // TODO: NOTIFICATION: send notification to LodgeOwner (HOST) that RequestForReservation is created

        requestAutomaticApproval(request, lodge, guest);

        return RequestForReservationMapper.INSTANCE.toDto(requestForReservationRepository.save(request));
    }

    private UserDto getLoggedInUser() {
        UUID id = AuthUtils.getLoggedUserId();
        UserDto user = restService.getUserById(id);
        if (user == null) {
            throw new NotFoundException("User doesn't exist");
        }
        return user;
    }

    private LodgeDto getLodge(UUID lodgeId) {
        LodgeDto lodge = restService.getLodgeById(lodgeId);
        if (lodge == null) {
            throw new NotFoundException("Lodge doesn't exist");
        }
        return lodge;
    }

    private List<LodgeAvailabilityPeriodDto> getLodgeAvailabilityPeriods(UUID lodgeId) {
        List<LodgeAvailabilityPeriodDto> availabilityPeriods = restService.getLodgeAvailabilityPeriods(lodgeId);
        if (availabilityPeriods == null) {
            throw new NotFoundException("LodgeAvailabilityPeriods doesn't exist");
        }
        return availabilityPeriods;
    }

    private void checkLoggedInUserIsGuest(UserDto user) {
        if (!user.getRole().equals("GUEST")) {
            throw new ForbiddenException("You are not logged in as guest.");
        }
    }

    private void createChecks(RequestForReservation request, LodgeDto lodge, List<LodgeAvailabilityPeriodDto> availabilityPeriods) {
        checkGuestNumberIsInRanges(request, lodge);
        checkDatesRangeIsValid(request.getDateFrom(), request.getDateTo());
        checkIfThereIsLodgeAvailabilityPeriodForRequest(request, availabilityPeriods);
        checkForExistingRequestForReservationWithOverlappingDateRange(request);
        checkForExistingReservationWithOverlappingDateRange(request);
    }

    private void checkGuestNumberIsInRanges(RequestForReservation request, LodgeDto lodge) {
        if (!( lodge.getMinimalGuestNumber() <= request.getNumberOfGuests() && request.getNumberOfGuests() <= lodge.getMaximalGuestNumber() )) {
            throw new BadRequestException("Number of guests is not in range.");
        }
    }

    private void checkDatesRangeIsValid(LocalDateTime dateFrom, LocalDateTime dateTo) {
        // Check if dateFrom is earlier than dateTo
        if (!dateFrom.isBefore(dateTo)) {
            throw new BadRequestException("DateFrom is not earlier than DateTo.");
        }
        // Check if there is at least one day between dateFrom and dateTo
        if (!dateFrom.plusDays(1).isBefore(dateTo)) {
            throw new BadRequestException("Between DateFrom and DateTo needs to be at least one day.");
        }
    }

    private void checkIfThereIsLodgeAvailabilityPeriodForRequest(RequestForReservation request, List<LodgeAvailabilityPeriodDto> availabilityPeriods) {
        LodgeAvailabilityPeriodDto availabilityPeriod = getLodgeAvailabilityPeriodCompatibleWithRequest(request, availabilityPeriods);
        if (availabilityPeriod == null) {
            throw new BadRequestException("There is no lodge availability period for selected date range.");
        }
    }

    public LodgeAvailabilityPeriodDto getLodgeAvailabilityPeriodCompatibleWithRequest(RequestForReservation request, List<LodgeAvailabilityPeriodDto> availabilityPeriods) {
        LodgeAvailabilityPeriodDto retVal = null;
        for (LodgeAvailabilityPeriodDto availabilityPeriod : availabilityPeriods) {
            if (availabilityPeriod.getDateFrom().isBefore(request.getDateFrom()) && availabilityPeriod.getDateTo().isAfter(request.getDateTo()) ) {
                retVal = availabilityPeriod;
                break;
            }
        }
        return retVal;
    }

    private void checkForExistingRequestForReservationWithOverlappingDateRange(RequestForReservation request) {
        List<RequestForReservation> requestsForReservation = getRequestsForReservationForLodge(request.getLodgeId());
        for (RequestForReservation existingRequest : requestsForReservation) {
            if (existingRequest.getStatus() == RequestForReservationStatus.APPROVED) {          // if existing request is approved check if it is overlapping with new request
                if (existingRequest.getDateTo().isAfter(request.getDateFrom())
                        && existingRequest.getDateFrom().isBefore(request.getDateTo())) {
                    throw new BadRequestException("There is approved request with overlapping dates with this one.");
                }
            }
        }

    }

    public List<RequestForReservation> getRequestsForReservationForLodge(UUID lodgeId) {
        return requestForReservationRepository.findByLodgeId(lodgeId);
    }

    private void checkForExistingReservationWithOverlappingDateRange(RequestForReservation request) {
        List<Reservation> activeReservations = reservationService.getActiveReservationsForLodge(request.getLodgeId());
        for (Reservation existingReservation : activeReservations) {
            if (existingReservation.getDateTo().isAfter(request.getDateFrom())
                    && existingReservation.getDateFrom().isBefore(request.getDateTo())) {
                throw new BadRequestException("There is active reservation for this lodge with overlapping dates with this one.");
            }
        }
    }

    private double calculatePrice(RequestForReservation request, List<LodgeAvailabilityPeriodDto> availabilityPeriods) {
        LodgeAvailabilityPeriodDto availabilityPeriod = getLodgeAvailabilityPeriodCompatibleWithRequest(request, availabilityPeriods);
        int numberOfDays = calculateDaysBetween(request.getDateFrom(), request.getDateTo());
        if (availabilityPeriod.getPriceType().equals("PER_GUEST")) {
            return calculatePricePerGuest(numberOfDays, request.getNumberOfGuests(), availabilityPeriod.getPrice());
        }
        else if (availabilityPeriod.getPriceType().equals("PER_LODGE")) {
            return calculatePricePerLodge(numberOfDays, availabilityPeriod.getPrice());
        }
        return 0.0;
    }

    private int calculateDaysBetween(LocalDateTime start, LocalDateTime end) {
        long daysBetween = ChronoUnit.DAYS.between(start, end);

        // Calculate hours and minutes difference
        long hoursBetween = ChronoUnit.HOURS.between(start, end);
        long minutesBetween = ChronoUnit.MINUTES.between(start, end);

        // Check if there are remaining hours/minutes that should cause rounding up
        if (hoursBetween % 24 > 0 || minutesBetween % (24 * 60) > 0) {
            daysBetween++;
        }

        return (int) daysBetween;
    }

    private double calculatePricePerGuest(int numberOfDays, int numberOfGuests, double specifiedPricePerGuest) {
        return numberOfDays * numberOfGuests * specifiedPricePerGuest;
    }

    private double calculatePricePerLodge(int numberOfDays, double specifiedPricePerLodge) {
        return numberOfDays * specifiedPricePerLodge;
    }

    private void requestAutomaticApproval(RequestForReservation request, LodgeDto lodge, UserDto guest) {
        if (!lodge.getApprovalType().equals("AUTOMATIC")) {
            return;
        }
        reservationService.createReservation(request);
        request.setStatus(RequestForReservationStatus.APPROVED);
        // TODO: NOTIFICATION: send notification to guest that host approved automatically reservation request

    }

    public void delete(UUID id) {
        UserDto guest = getLoggedInUser();
        checkLoggedInUserIsGuest(guest);
        RequestForReservation requestForReservation = getRequestForReservation(id);
        checkIfGuestMadeRequestForReservation(guest, requestForReservation);
        checkIfStatusEnablesDeletion(requestForReservation);
        requestForReservationRepository.deleteById(requestForReservation.getId());
    }

    private RequestForReservation getRequestForReservation(UUID id) {
        return requestForReservationRepository.findById(id).orElseThrow(() -> new NotFoundException("Request For Reservation doesn't exist"));
    }

    private void checkIfGuestMadeRequestForReservation(UserDto guest, RequestForReservation requestForReservation) {
        if (!guest.getId().equals(requestForReservation.getGuestId())) {
            throw new ForbiddenException("You can't delete Request for reservation you didn't made.");
        }
    }

    private void checkIfStatusEnablesDeletion(RequestForReservation requestForReservation) {
        if (!requestForReservation.getStatus().equals(RequestForReservationStatus.WAITING_FOR_RESPONSE)) {
            throw new BadRequestException("Request for reservation can be deleted only when it has status waiting for response.");
        }
    }

    public RequestForReservationDto update(UUID id, RequestForReservationStatusUpdateRequest updateRequest) {
        RequestForReservation request = getRequestForReservation(id);
        checkIfRequestForReservationStatusCanBeUpdated(request);
        UserDto owner = getLoggedInUser();
        LodgeDto lodge = getLodge(request.getLodgeId());
        checkIfLoggedInUserIsLodgeOwner(owner, lodge);
        checkIfApprovalTypeForLodgeIsManual(lodge);
        RequestForReservationMapper.INSTANCE.update(request, updateRequest);
        checkIfRequestForReservationStatusIsValidAfterUpdate(request);
        RequestForReservation updatedRequest = requestForReservationRepository.save(request);
        if (updatedRequest.getStatus() == RequestForReservationStatus.APPROVED) {
            requestForReservationApproved(updatedRequest);
        }
        else {      // RequestForReservationStatus.DENIED
            requestForReservationDenied();
        }
        return RequestForReservationMapper.INSTANCE.toDto(updatedRequest);
    }

    private void checkIfLoggedInUserIsLodgeOwner(UserDto owner, LodgeDto lodge) {
        if (!lodge.getOwnerId().equals(owner.getId())) {
            throw new BadRequestException("You can change status of Request for reservation only for Lodges you own.");
        }
    }

    private void checkIfRequestForReservationStatusCanBeUpdated(RequestForReservation request) {
        if (request.getStatus() != RequestForReservationStatus.WAITING_FOR_RESPONSE) {
            throw new BadRequestException("You can update status of Request for reservation only when status is WAITING_FOR_RESPONSE.");
        }
    }

    private void checkIfRequestForReservationStatusIsValidAfterUpdate(RequestForReservation request) {
        if (request.getStatus() == RequestForReservationStatus.WAITING_FOR_RESPONSE) {
            throw new BadRequestException("You can update status of Request for reservation only to APPROVED or DENIED status.");
        }
    }

    private void checkIfApprovalTypeForLodgeIsManual(LodgeDto lodge) {
        if (!lodge.getApprovalType().equals("MANUAL")) {
            throw new BadRequestException("You can't update status of Request for reservation manually because it is not selected for this Lodge.");
        }
    }

    private void requestForReservationApproved(RequestForReservation request) {
        reservationService.createReservation(request);
        // TODO: NOTIFICATION: send notification to guest that host approved manually reservation request
    }

    private void requestForReservationDenied() {
        System.out.println("DENIED Request");
        // TODO: NOTIFICATION: send notification to guest that host denied manually reservation request
    }

    public List<RequestForReservationDto> getHostReservationRequests() {
        UserDto host = getLoggedInUser();
        List<RequestForReservation> requests = requestForReservationRepository.findByOwnerId(host.getId());
        return RequestForReservationMapper.INSTANCE.toDto(requests);
    }

    public List<RequestForReservationDto> getGuestReservationRequests() {
        UserDto guest = getLoggedInUser();
        List<RequestForReservation> requests = requestForReservationRepository.findByGuestId(guest.getId());
        return RequestForReservationMapper.INSTANCE.toDto(requests);
    }

    public RequestForReservationDto getReservationRequestByIdHost(UUID id) {
        UserDto host = getLoggedInUser();
        RequestForReservation request = requestForReservationRepository.findById(id).orElseThrow(() -> new NotFoundException("Request For Reservation doesn't exist"));
        if (!request.getOwnerId().equals(host.getId())) {
            throw new ForbiddenException("You can only get reservation requests for lodges you own.");
        }
        return RequestForReservationMapper.INSTANCE.toDto(request);
    }

    public RequestForReservationDto getReservationRequestByIdGuest(UUID id) {
        UserDto guest = getLoggedInUser();
        RequestForReservation request = requestForReservationRepository.findById(id).orElseThrow(() -> new NotFoundException("Request For Reservation doesn't exist"));
        if (!request.getGuestId().equals(guest.getId())) {
            throw new ForbiddenException("You can only get reservation requests for lodges you made requests for.");
        }
        return RequestForReservationMapper.INSTANCE.toDto(request);
    }

    public void cancelRequestForReservation(UUID requestForReservationId) {
        RequestForReservation request = requestForReservationRepository.findById(requestForReservationId).orElseThrow(() -> new NotFoundException("Request For Reservation doesn't exist"));
        request.setStatus(RequestForReservationStatus.CANCELED);
        requestForReservationRepository.save(request);
    }

}
