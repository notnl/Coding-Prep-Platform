
package com.codingprep.features.matchmaking.repository;

import java.util.UUID;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.codingprep.features.matchmaking.dto.LiveMatchStateDTO;
import java.util.List;
import java.util.Optional;


@Repository
public interface LiveMatchStateRepository extends CrudRepository<LiveMatchStateDTO,UUID > {
    Optional<LiveMatchStateDTO> findByRoomCode(String roomCode);
}
