package com.codingprep.features.matchmaking.service;

import com.codingprep.features.matchmaking.dto.*;
import com.codingprep.features.submission.models.SubmissionStatus;

import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface MatchService {

    CreateMatchResponse createMatch(CreateMatchRequest request, Long creatorId,String creatorUsername);

    JoinMatchResponse joinMatch(JoinMatchRequest request, Long joiningUserId,String joiningUsername);

    LobbyStateDTO getLobbyState(UUID matchId);

    List<LobbyDetailsDTO> getAllLobby();

    void startLobbyCountdown(UUID matchId);

    MatchStateResponseDTO getMatchState(UUID matchId);

   void processSubmissionResult(UUID matchId, Long userId, SubmissionStatus submissionStatus);

   void switchTeamMatch(UUID matchId, Long userId,int toTeam);

   void hostStartDiscussion(UUID matchId, Long userId);
   int hostNextProblem(UUID matchId, Long userId);

   void hostEndMatch(UUID matchId, Long userId);

    DiscussionDetailsResponse getTeamDiscussionCode(UUID matchId,long userId);
    List<Long> getTeamPoints(UUID matchId);

  //  void completeMatch(UUID matchId);

    //MatchResultDTO getMatchResults(UUID matchId);

    //PageDto<PastMatchDto> getPastMatchesForUser(Long userId, String result, Pageable pageable);

}
