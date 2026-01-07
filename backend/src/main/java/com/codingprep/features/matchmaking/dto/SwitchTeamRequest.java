
package com.codingprep.features.matchmaking.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SwitchTeamRequest {

    @NotBlank(message = "Team to switch required")
    private int toTeam;
}
