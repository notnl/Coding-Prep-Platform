
package com.codingprep.features.matchmaking.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class JoinMatchRequest {

    @NotBlank(message = "Match ID is required")
    private UUID matchId;
}
