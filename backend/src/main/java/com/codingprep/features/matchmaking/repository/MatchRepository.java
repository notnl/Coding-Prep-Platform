package com.codingprep.features.matchmaking.repository;

import org.postgresql.replication.fluent.ReplicationCreateSlotBuilder;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.codingprep.features.matchmaking.models.MatchRoom;
import com.codingprep.features.matchmaking.models.MatchStatus;

@Repository
public interface MatchRepository extends JpaRepository<MatchRoom,UUID> {  


    @Query(value="SELECT * FROM match",nativeQuery=true)
    List<MatchRoom> findAllMatchRoom();
            
    Optional<MatchRoom> findByRoomCode(String roomCode);
    Optional<MatchRoom> findById(UUID id);

    List<MatchRoom> findAllByStatusAndScheduledAtBefore(MatchStatus status, Instant currentTime);


    List<MatchRoom> findAllByStatus(MatchStatus status);


    List<MatchRoom> findAllByStatusAndCreatedAtBefore(MatchStatus status, Instant cutoff);
    
    





}




