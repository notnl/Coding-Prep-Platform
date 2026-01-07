


package com.codingprep.features.matchmaking.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.IdClass;




@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@RedisHash("discussion")
public class DiscussionDetails {

    @Id
    private String id;

    private UUID matchId;
    private int teamIndex;

    
    private Map<Long,PlayerDiscussionIdentification> playerDiscussionList;

     public static String buildId(UUID matchId, int teamIndex) {
        return "discussion:" + matchId + ":" + teamIndex;
    }

}
