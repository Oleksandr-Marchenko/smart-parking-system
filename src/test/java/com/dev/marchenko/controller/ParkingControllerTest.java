package com.dev.marchenko.controller;

import com.dev.marchenko.domain.vehicle.VehicleType;
import com.dev.marchenko.dto.CheckInRequest;
import com.dev.marchenko.dto.CheckOutResponse;
import com.dev.marchenko.dto.TicketResponse;
import com.dev.marchenko.service.ParkingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.matchesPattern;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ParkingController.class)
public class ParkingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ParkingService parkingService;

    @Test
    void checkIn_ShouldReturnTicket() throws Exception {
        CheckInRequest request = new CheckInRequest("AA1111BB", VehicleType.CAR);
        TicketResponse response = TicketResponse.builder()
                .ticketId(1L)
                .licensePlate("AA1111BB")
                .vehicleType("CAR")
                .entryTime(LocalDateTime.now())
                .slotNumber("A-1")
                .levelFloor(1)
                .build();

        when(parkingService.checkIn("AA1111BB", VehicleType.CAR)).thenReturn(response);

        mockMvc.perform(post("/api/v1/parking/check-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ticketId").value(1L))
                .andExpect(jsonPath("$.licensePlate").value("AA1111BB"))
                .andExpect(jsonPath("$.slotNumber").value("A-1"));
    }

    @Test
    void checkOut_ShouldReturnCorrectJson() throws Exception {
        Long ticketId = 1L;
        LocalDateTime now = LocalDateTime.now();

        CheckOutResponse response = CheckOutResponse.builder()
                .licensePlate("AA1111BB")
                .entryTime(now.minusHours(2))
                .exitTime(now)
                .durationMinutes(120L)
                .totalFee(new BigDecimal("20.50"))
                .build();

        when(parkingService.checkOut(ticketId)).thenReturn(response);

        mockMvc.perform(post("/api/v1/parking/check-out/{ticketId}", ticketId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.licensePlate").value("AA1111BB"))
                .andExpect(jsonPath("$.entryTime").value(matchesPattern("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}")))
                .andExpect(jsonPath("$.durationMinutes").value(120))
                .andExpect(jsonPath("$.totalFee").value(20.50));
    }

    @Test
    void getActiveSessions_ShouldReturnList() throws Exception {
        TicketResponse ticket = TicketResponse.builder()
                .ticketId(1L)
                .licensePlate("AA1111BB")
                .build();

        when(parkingService.getActiveSessions()).thenReturn(List.of(ticket));

        mockMvc.perform(get("/api/v1/parking/sessions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].ticketId").value(1L))
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void checkIn_ValidationFailed() throws Exception {
        CheckInRequest invalidRequest = new CheckInRequest("", VehicleType.CAR);

        mockMvc.perform(post("/api/v1/parking/check-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }
}