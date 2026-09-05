package com.example.travel.trip;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.travel.auth.dto.LoginRequest;
import com.example.travel.auth.dto.RegisterRequest;
import com.example.travel.support.AbstractIntegrationTest;
import com.example.travel.trip.dto.CreateFlightSegmentRequest;
import com.example.travel.trip.dto.CreateHotelBookingRequest;
import com.example.travel.trip.dto.CreateTravelerRequest;
import com.example.travel.trip.dto.CreateTripRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

class TripFlowIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createTrip_withFlightsAndHotels_derivesDatesAndSupportsFullLifecycle() throws Exception {
        String accessToken = registerAndLogin("trip.owner@example.com");

        Instant departure = Instant.now().plus(10, ChronoUnit.DAYS);
        Instant arrival = departure.plus(6, ChronoUnit.HOURS);
        Instant checkIn = departure.plus(1, ChronoUnit.DAYS);
        Instant checkOut = checkIn.plus(3, ChronoUnit.DAYS);

        CreateTripRequest createRequest = new CreateTripRequest(
                "Summer trip",
                "A relaxing summer trip",
                "Paris",
                List.of(new CreateFlightSegmentRequest("Air France", "AF123", "JFK", "CDG", departure, arrival)),
                List.of(new CreateHotelBookingRequest("Hotel Lumiere", "Paris", "1 Rue de Paris", checkIn, checkOut, "CONF123")));

        String createResponse = mockMvc.perform(post("/api/v1/trips")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PLANNED"))
                .andExpect(jsonPath("$.flights.length()").value(1))
                .andExpect(jsonPath("$.hotels.length()").value(1))
                .andExpect(jsonPath("$.startDateTime").isNotEmpty())
                .andExpect(jsonPath("$.endDateTime").isNotEmpty())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode tripNode = objectMapper.readTree(createResponse);
        long tripId = tripNode.get("id").asLong();

        // start should be the earlier of flight departure / hotel check-in, end the later of
        // flight arrival / hotel check-out
        assertInstantEquals(departure, tripNode.get("startDateTime").asText());
        assertInstantEquals(checkOut, tripNode.get("endDateTime").asText());

        mockMvc.perform(post("/api/v1/trips/{tripId}/travelers", tripId)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateTravelerRequest(
                                "Jane", "Doe", "jane.traveler@example.com", LocalDate.of(1990, 1, 1), null, null))))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/trips/{tripId}", tripId).header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.travelers.length()").value(1));

        mockMvc.perform(get("/api/v1/trips")
                        .header("Authorization", "Bearer " + accessToken)
                        .param("destination", "Paris"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1));

        mockMvc.perform(put("/api/v1/trips/{tripId}", tripId)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Updated trip\",\"description\":\"Updated\",\"destination\":\"Paris\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated trip"));

        mockMvc.perform(delete("/api/v1/trips/{tripId}", tripId).header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/trips/{tripId}", tripId).header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void otherUsersCannotAccessSomeoneElsesTrip() throws Exception {
        String ownerToken = registerAndLogin("owner2@example.com");
        String otherToken = registerAndLogin("intruder@example.com");

        CreateTripRequest createRequest = new CreateTripRequest("Private trip", null, "Tokyo", List.of(), List.of());
        String createResponse = mockMvc.perform(post("/api/v1/trips")
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        long tripId = objectMapper.readTree(createResponse).get("id").asLong();

        mockMvc.perform(get("/api/v1/trips/{tripId}", tripId).header("Authorization", "Bearer " + otherToken))
                .andExpect(status().isForbidden());
    }

    private void assertInstantEquals(Instant expected, String actualText) {
        Instant actual = Instant.parse(actualText);
        org.assertj.core.api.Assertions.assertThat(actual).isEqualTo(expected.truncatedTo(ChronoUnit.MICROS));
    }

    private String registerAndLogin(String email) throws Exception {
        RegisterRequest register = new RegisterRequest("Test", "User", email, "password123", null);
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(register)))
                .andExpect(status().isCreated());

        String loginResponse = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest(email, "password123"))))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(loginResponse).get("accessToken").asText();
    }
}
