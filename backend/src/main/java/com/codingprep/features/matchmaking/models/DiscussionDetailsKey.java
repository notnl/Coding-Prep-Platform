
package com.codingprep.features.matchmaking.models;

import java.io.Serializable;
import java.util.UUID;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor

public class DiscussionDetailsKey implements Serializable {
    private UUID matchId;
    private int teamIndex;
     

}
