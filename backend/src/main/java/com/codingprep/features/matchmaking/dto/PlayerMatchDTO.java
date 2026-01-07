package com.codingprep.features.matchmaking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.ElementCollection;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlayerMatchDTO implements Serializable {

    
    public Long player_id;      
    public String player_username;
    public int player_team;



}
