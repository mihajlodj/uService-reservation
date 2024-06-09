package ftn.reservationservice.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import ftn.reservationservice.AuthPostgresIntegrationTest;
import ftn.reservationservice.domain.dtos.LodgeAvailabilityPeriodDto;
import ftn.reservationservice.domain.dtos.LodgeDto;
import ftn.reservationservice.domain.dtos.RequestForReservationCreateRequest;
import ftn.reservationservice.domain.dtos.UserDto;
import ftn.reservationservice.services.RestService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@Sql("/sql/reservation.sql")
public class RequestForReservationControllerTest extends AuthPostgresIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RestService restService;

    @BeforeEach
    public void setup() {
        authenticateGuest();
    }

    @Test
    public void testCreateRequestForReservationSuccessful() throws Exception{
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

        mockMvc.perform(post("/api/reservation/requestforreservation")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.lodgeId").value(lodgeId))
                .andExpect(jsonPath("$.guestId").value(userId))
                .andExpect(jsonPath("$.ownerId").value(lodgeOwnerId))
                .andExpect(jsonPath("$.price").value(120.0))
                .andExpect(jsonPath("$.dateFrom").value("2024-05-02 20:10:21.2632212"))
                .andExpect(jsonPath("$.dateTo").value("2024-05-04 20:10:21.2632212"))
                .andExpect(jsonPath("$.numberOfGuests").value(request.getNumberOfGuests()))
                .andExpect(jsonPath("$.status").value("APPROVED"));

    }

    @Test
    public void testDeleteRequestForReservationSuccessful() throws Exception {
        String userId = "e49fcaa5-d45b-4556-9d91-13e58187fea6";
        mockGuest(userId);

        String requestForReservationId = "b86553e1-2552-41cb-9e40-7eeeee424891";

        mockMvc.perform(delete("/api/reservation/requestforreservation/" + requestForReservationId)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

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

}
