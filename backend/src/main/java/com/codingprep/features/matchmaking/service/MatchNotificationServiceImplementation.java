
package com.codingprep.features.matchmaking.service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.codingprep.features.matchmaking.dto.CountdownStartPayload;
import com.codingprep.features.matchmaking.models.MatchRoom;
import com.codingprep.features.matchmaking.models.MatchStatus;
import com.codingprep.features.matchmaking.repository.MatchRepository;

import lombok.RequiredArgsConstructor;


@Service
public class MatchNotificationServiceImplementation implements MatchNotificationService{


    private final SimpMessagingTemplate messagingTemplate;

    private static final String MATCH_TOPIC_PREFIX = "/topic/match/";
    private static final String COUNTDOWN_TOPIC_SUFFIX = "/countdown";
    private static final String MATCH_POINTS_SUFFIX = "/points";


    private String getMatchTopic(UUID matchId) {
        return MATCH_TOPIC_PREFIX + matchId;
    }

    private String GET_COUNTDOWN_TOPIC_MATCH(UUID matchId){ 
        return MATCH_TOPIC_PREFIX + matchId + COUNTDOWN_TOPIC_SUFFIX;

    }

    public MatchNotificationServiceImplementation(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @Override
    public void notifyPlayerJoined(UUID matchId,String eventType) {
        String destination = getMatchTopic(matchId);

        Map<String, Object> message = Map.of(
            "eventType", eventType
            );
        messagingTemplate.convertAndSend(destination,(Object)message);
    }

    @Override
    public void notifyMatchUpdate(UUID matchId,String eventType){

        String destination = getMatchTopic(matchId);

        Map<String, Object> message = Map.of(
            "eventType", eventType
            );
        messagingTemplate.convertAndSend(destination,(Object)message);


    }

    @Override
    public void notifyPointsUpdate(UUID matchId){

        String destination = getMatchTopic(matchId) + MATCH_POINTS_SUFFIX;

        //Map<String, Object> message = Map.of(
        //    "eventType", ""
        //    );

        messagingTemplate.convertAndSend(destination, "");


    }


    @Override
    public void notifyCountdownStarted(UUID matchId, String countdownType, CountdownStartPayload payload) {
        String destination = GET_COUNTDOWN_TOPIC_MATCH(matchId);
        Map<String, Object> message = Map.of(
            "eventType", countdownType,
            "payload", payload,
            "timestamp", System.currentTimeMillis()
        );

        System.out.println("[WS_NOTIFY matchId={}] -> Sending COUNTDONW_STARTED  event." +  payload);
        messagingTemplate.convertAndSend(destination, (Object)message);
    }


    @Override
    public void notifyMatchStart(UUID matchId) {
        String destination = getMatchTopic(matchId);
        Map<String, Object> payload = Map.of(
            "eventType", "MATCH_START",
            "timestamp", System.currentTimeMillis()
        );

        System.out.println("[WS_NOTIFY matchId={}] -> Sending MATCH_START event." +  matchId);

        messagingTemplate.convertAndSend(destination, (Object)payload);
    }
    


}
