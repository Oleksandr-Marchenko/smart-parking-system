package com.dev.marchenko.controller;

import com.dev.marchenko.domain.lot.Level;
import com.dev.marchenko.domain.lot.ParkingLot;
import com.dev.marchenko.domain.slot.ParkingSlot;
import com.dev.marchenko.domain.slot.SlotType;
import com.dev.marchenko.dto.*;
import com.dev.marchenko.mapper.ParkingMapper;
import com.dev.marchenko.service.AdminService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminController.class)
public class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AdminService adminService;

    @MockitoBean
    private ParkingMapper mapper;

    @Test
    void createLot_ShouldReturnCreated() throws Exception {
        ParkingLotRequest request = new ParkingLotRequest("Main Lot");
        ParkingLotResponse response = new ParkingLotResponse(1L, "Main Lot");

        when(adminService.createLot(any())).thenReturn(new ParkingLot());
        when(mapper.toLotResponse(any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/admin/lots")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Main Lot"));
    }

    @Test
    void removeLot_ShouldReturnNoContent() throws Exception {
        doNothing().when(adminService).removeLot(1L);

        mockMvc.perform(delete("/api/v1/admin/lots/1"))
                .andExpect(status().isNoContent());

        verify(adminService).removeLot(1L);
    }

    @Test
    void addLevel_ShouldReturnCreated() throws Exception {
        LevelRequest request = new LevelRequest(1);
        LevelResponse response = new LevelResponse(1L, 1, 10L);

        when(adminService.addLevel(eq(1L), any())).thenReturn(new Level());
        when(mapper.toLevelResponse(any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/admin/lots/1/levels")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.floorNumber").value(1));
    }

    @Test
    void addSlot_ShouldReturnCreated() throws Exception {
        SlotRequest request = new SlotRequest("A1", SlotType.COMPACT);
        SlotResponse response = new SlotResponse(1L, "A1", SlotType.COMPACT.name(), true);

        when(adminService.addSlot(eq(1L), any())).thenReturn(new ParkingSlot());
        when(mapper.toSlotResponse(any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/admin/levels/1/slots")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.slotNumber").value("A1"));
    }

    @Test
    void toggleSlot_ShouldReturnOk() throws Exception {
        SlotResponse response = new SlotResponse(1L, "A1", SlotType.COMPACT.name(), false);

        when(adminService.updateSlotAvailability(1L, false)).thenReturn(new ParkingSlot());
        when(mapper.toSlotResponse(any())).thenReturn(response);

        mockMvc.perform(patch("/api/v1/admin/slots/1/availability")
                        .param("available", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isAvailable").value(false));
    }

    @Test
    void createLot_ValidationFailed() throws Exception {
        // Предположим, в ParkingLotRequest стоит @NotBlank на поле name
        ParkingLotRequest request = new ParkingLotRequest("");

        mockMvc.perform(post("/api/v1/admin/lots")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}