package com.dev.marchenko.dto;

import com.dev.marchenko.domain.slot.SlotType;

public record SlotRequest(String slotNumber, SlotType type) {}
