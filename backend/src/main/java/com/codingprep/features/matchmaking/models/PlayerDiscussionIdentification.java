
package com.codingprep.features.matchmaking.models;

import java.io.Serializable;

import lombok.Builder;

@Builder
public record PlayerDiscussionIdentification(Long playerId, String playerName, String playerCode) implements Serializable { 

    
}
