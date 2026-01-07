package com.codingprep.features.matchmaking.dto;


import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.codingprep.features.matchmaking.models.MatchStatus;
import com.codingprep.features.problem.dto.ProblemDetailResponse;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MatchStateResponseDTO {

    private UUID matchId;
    private MatchStatus matchStatus;
    private Long hostId;
    private Integer durationInMinutes;
    private Instant startedAt;
    @JdbcTypeCode(SqlTypes.JSON)
    private List<ProblemDetailResponse> problemDetails;
    private Integer currentProblem;
    private List<Long> teamScores;


}
/*
export interface LiveMatchState {
    matchId: string;
    startedAt: string;
    durationInMinutes: number;
}
export interface ArenaData {
    matchId: string;
    startedAt: string;
    durationInMinutes: number;
    problemDetails: ProblemDetail[];
    
}
*/
