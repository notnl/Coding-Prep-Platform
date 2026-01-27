package com.codingprep.features.matchmaking.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class JoinMatchRoomCodeRequest {
    @NotBlank(message = "Room Code is required")
    private String roomCode;
}
