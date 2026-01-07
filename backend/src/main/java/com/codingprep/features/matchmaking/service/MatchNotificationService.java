package com.codingprep.features.matchmaking.service;

import com.codingprep.features.matchmaking.dto.CountdownStartPayload;
//import com.codingprep.features.match.dto.LiveMatchStateDTO;
//import com.codingprep.features.match.dto.MatchResultDTO;

import java.util.UUID;

public interface MatchNotificationService {

    void notifyPlayerJoined(UUID matchId,String eventType);

    void notifyMatchUpdate(UUID matchId,String eventType);

    //void notifyMatchEnd(UUID matchId, MatchResultDTO result);

    void notifyMatchStart(UUID matchId);

    void notifyPointsUpdate(UUID matchId);

    //void notifyMatchCanceled(UUID matchId, String reason);

    void notifyCountdownStarted(UUID matchId, String countdownType, CountdownStartPayload payload);
}
