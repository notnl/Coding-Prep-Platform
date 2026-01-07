package com.codingprep.features.matchmaking.models;


import jakarta.persistence.*;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.codingprep.features.matchmaking.dto.PlayerMatchDTO;
import com.codingprep.features.matchmaking.models.MatchStatus;

import java.time.Instant;
import java.util.UUID;
import java.util.Collection;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

//import jakarta.persistence.Entity;
//
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity 
@Table(name = "match")
public class MatchRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "host_id", nullable = false, unique = true)
    private Long host_id;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MatchStatus status;


    @Column(name = "player_count", nullable = false,columnDefinition = "integer default 0")
    private int player_count;

    @Column(name = "max_player_count", nullable = false,columnDefinition = "integer default 64")
    private int max_player_count;   

   // @Column(name = "all_players", nullable = true)
   // @JdbcTypeCode(SqlTypes.JSON)
   // private List<PlayerMatchDTO> all_players;

    @Column(name = "room_code", nullable = false, unique = true)
    private String roomCode;

    @Column(name = "duration_in_minutes", nullable = false)
    private Integer durationInMinutes;

    @Column(name = "start_delay_in_seconds", nullable = false)
    private Integer startDelayInSecond;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @Column(name = "scheduled_at")
    private Instant scheduledAt;

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "ended_at")
    private Instant endedAt;

    
    
}


