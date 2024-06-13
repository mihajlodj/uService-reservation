package ftn.reservationservice.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import ftn.reservationservice.AuthPostgresIntegrationTest;
import ftn.reservationservice.domain.dtos.LodgeDto;
import ftn.reservationservice.domain.dtos.UserDto;
import ftn.reservationservice.repositories.ReservationRepository;
import ftn.reservationservice.services.ReservationService;
import ftn.reservationservice.services.RestService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@Sql("/sql/reservation2.sql")
public class ReservationControllerTest extends AuthPostgresIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

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
    public void getCountCanceledReservationsByGuestNoCanceledReservations() throws Exception {
        String guestId = "e49fcaa5-d45b-4556-9d91-13e58187fea6";

        mockMvc.perform(get("/api/reservation/canceled/count/" + guestId)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.count").value(0));

    }

    @Test
    public void getCountCanceledReservationsByGuestOneCanceledReservation() throws Exception {
        String guestId = "e49fcaa5-d45b-4556-9d91-13e58187fef9";

        mockMvc.perform(get("/api/reservation/canceled/count/" + guestId)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.count").value(2));

    }

    @Test
    public void testGetMyReservationsHostSuccess() throws Exception {
        authenticateHost();

        String lodgeOwnerId = "e49fcab5-d45b-4556-9d91-14e58177fea6";
        mockOwner(lodgeOwnerId);

        mockMvc.perform(get("/api/reservation/all/host")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)));

    }

    @Test
    public void testGetMyReservationsGuestSuccess() throws Exception {
        authenticateGuest();
        String guestId = "e49fcaa5-d45b-4556-9d91-13e58187fea6";
        mockGuest(guestId);

        mockMvc.perform(get("/api/reservation/all/guest")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)));

    }

    @Test
    public void testGetReservationsForLodgeSuccess() throws Exception {
        String lodgeId = "b86553e1-2552-41cb-9e40-7ef87c424850";
        String lodgeOwnerId = "e49fcab5-d45b-4556-9d91-14e58177fea6";

        mockOwner(lodgeOwnerId);
        mockLodgeAutomaticApproval(lodgeId, lodgeOwnerId);

        mockMvc.perform(get("/api/reservation/all/" + lodgeId)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)));

    }

    @Test
    public void testGetReservationByIdHostSuccess() throws Exception {
        String lodgeOwnerId = "e49fcab5-d45b-4556-9d91-14e58177fea6";
        mockOwner(lodgeOwnerId);

        String reservationId = "b86553e1-2552-41cb-9e40-7aaaaa424850";

        mockMvc.perform(get("/api/reservation/host/" + reservationId)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(reservationId))
                .andExpect(jsonPath("$.lodgeId").value("b86553e1-2552-41cb-9e40-7ef87c424850"))
                .andExpect(jsonPath("$.guestId").value("e49fcaa5-d45b-4556-9d91-13e58187fea6"))
                .andExpect(jsonPath("$.ownerId").value(lodgeOwnerId))
                .andExpect(jsonPath("$.price").value(99.99))
                .andExpect(jsonPath("$.dateFrom").value("2024-05-19"))
                .andExpect(jsonPath("$.dateTo").value("2024-05-23"))
                .andExpect(jsonPath("$.numberOfGuests").value(2))
                .andExpect(jsonPath("$.status").value("ACTIVE"));

    }

    @Test
    public void testGetReservationByIdGuestSuccess() throws Exception {
        String guestId = "e49fcaa5-d45b-4556-9d91-13e58187fea6";
        mockGuest(guestId);

        String lodgeOwnerId = "e49fcab5-d45b-4556-9d91-14e58177fea6";
        String reservationId = "b86553e1-2552-41cb-9e40-7aaaaa424850";

        mockMvc.perform(get("/api/reservation/guest/" + reservationId)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(reservationId))
                .andExpect(jsonPath("$.lodgeId").value("b86553e1-2552-41cb-9e40-7ef87c424850"))
                .andExpect(jsonPath("$.guestId").value("e49fcaa5-d45b-4556-9d91-13e58187fea6"))
                .andExpect(jsonPath("$.ownerId").value(lodgeOwnerId))
                .andExpect(jsonPath("$.price").value(99.99))
                .andExpect(jsonPath("$.dateFrom").value("2024-05-19"))
                .andExpect(jsonPath("$.dateTo").value("2024-05-23"))
                .andExpect(jsonPath("$.numberOfGuests").value(2))
                .andExpect(jsonPath("$.status").value("ACTIVE"));

    }

    @Test
    public void testCheckIfUserHadReservationInLodgeSuccess() throws Exception {
        authenticateAdmin();
        UUID guestId = UUID.fromString("e49fcaa5-d45b-4556-9d91-13e58187fea6");
        UUID lodgeId = UUID.fromString("b86553e1-2552-41cb-9e40-7ef87c424850");

        mockMvc.perform(get("/api/reservation/check/userhadreservation/" + guestId + "/" + lodgeId)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.value").value(true));

    }

    @Test
    public void testCheckIfUserHadReservationInLodgeGuestNotFound() throws Exception {
        authenticateAdmin();
        UUID guestId = UUID.fromString("e49fcaa5-d45b-4556-9d91-13e58187fea7");
        UUID lodgeId = UUID.fromString("b86553e1-2552-41cb-9e40-7ef87c424850");

        mockMvc.perform(get("/api/reservation/check/userhadreservation/" + guestId + "/" + lodgeId)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.value").value(false));
    }

    @Test
    public void testCheckIfUserHadReservationInLodgeLodgeNotFound() throws Exception {
        authenticateAdmin();
        UUID guestId = UUID.fromString("e49fcaa5-d45b-4556-9d91-13e58187fea6");
        UUID lodgeId = UUID.fromString("b86553e1-2552-41cb-9e40-7ef87c424857");

        mockMvc.perform(get("/api/reservation/check/userhadreservation/" + guestId + "/" + lodgeId)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.value").value(false));
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
