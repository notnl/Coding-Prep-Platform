

package com.codingprep.features.matchmaking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

import com.codingprep.features.matchmaking.models.MatchStatus;
import com.codingprep.features.problem.models.Problem;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@RedisHash("liveMatch")
public class LiveMatchStateDTO {

    @Id
    private UUID matchId;

    private MatchStatus matchStatus;

    @Indexed
    private String roomCode;
    private String roomName;

    private Instant startedAt; // Time elapsed from arena start
    private Instant scheduledAt; // The actual scheduled time to start from lobby to arena
    private Integer startDelayInSecond; // Time to start from lobby to arena
    private Integer durationInMinutes; // How long the arena lasts 
                                       //
    private int max_player_count;   

    private Long hostId;
    private Integer currentProblem; // this is 0-indexed  for current problem shown by the match state
    private Integer maxProblemCount; // number of problems
                                     //
                                     //
    private List<UUID> allProblems; // We store UUID of the problem  
                                    //
}
