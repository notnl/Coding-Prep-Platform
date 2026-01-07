package com.codingprep.features.matchmaking.dto;

import com.codingprep.features.matchmaking.models.MatchStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;
import java.util.Collection;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LobbyStateDTO {
    private UUID matchId;
    private List<PlayerMatchDTO> allPlayers;
    private MatchStatus status;
    private Instant scheduledAt;
    private Integer durationInMinutes;
    private Long hostId;
    private Integer maxPlayers;
}
