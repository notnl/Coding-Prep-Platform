
package com.codingprep.features.matchmaking.repository;

import java.util.UUID;

import org.springframework.data.repository.CrudRepository;

import com.codingprep.features.matchmaking.dto.LiveMatchStateDTO;

public interface LiveMatchStateRepository extends CrudRepository<LiveMatchStateDTO,UUID > {

}
