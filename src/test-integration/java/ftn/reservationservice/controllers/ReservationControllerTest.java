package ftn.reservationservice.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import ftn.reservationservice.AuthPostgresIntegrationTest;
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
                .andExpect(jsonPath("$.count").value(1));

    }

}