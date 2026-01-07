package com.codingprep.features.redis.service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.codingprep.features.matchmaking.dto.LiveMatchStateDTO;
import com.codingprep.features.matchmaking.dto.PlayerMatchDTO;
import com.codingprep.features.matchmaking.models.DiscussionDetails;
import com.codingprep.features.matchmaking.models.PlayerDiscussionIdentification;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MatchRedisService {


    //private final RedisTemplate<String, LiveMatchStateDTO> liveMatchTemplate;

    private final RedisTemplate<String, PlayerMatchDTO> playerTemplate;

    private final RedisTemplate<String, Long> scoreTemplate;

    private final RedisTemplate<String, PlayerDiscussionIdentification> playerDiscussionTemplate;

    private ObjectMapper objectMapper;

    public void addPlayer(UUID matchId, PlayerMatchDTO player) {
        String key = "liveMatch:" + matchId + ":players";

        playerTemplate.opsForHash()
                .put(key, player.getPlayer_id().toString(), player);
    }
    public List<PlayerMatchDTO> getAllPlayers(UUID matchId) {

        String key = "liveMatch:" + matchId + ":players";

        return playerTemplate.opsForHash().values(key).stream().filter(PlayerMatchDTO.class::isInstance).map(
                PlayerMatchDTO.class::cast).toList();

    }
    public void createTeamScore(UUID matchId, int teamIndex,Long byScore) { 

        String key = "liveMatch:" + matchId + ":scores";
        scoreTemplate.opsForHash().put(key, Integer.toString(teamIndex), byScore);
        scoreTemplate.expire(key, Duration.ofHours(6L)); // After a set time just get rid of the score, don't really expect a live session to last longer than that

    }
    public void increaseTeamScore(UUID matchId, int teamIndex,Long byScore){

        String key = "liveMatch:" + matchId + ":scores";

        scoreTemplate.opsForHash().increment(key, Integer.toString(teamIndex), byScore);

    }

    public void updatePlayerDiscussion(UUID matchId,Long userId,String playerName, int teamIndex,String code){

        String key = "discussion:" + matchId + ":" + teamIndex;

        
        PlayerDiscussionIdentification pD = PlayerDiscussionIdentification.builder().playerCode(code).playerId(userId).playerName(playerName).build();

        playerDiscussionTemplate.opsForHash().put(key,Long.toString(userId),pD);

    }

    public List<PlayerDiscussionIdentification> getAllPlayerDiscussion(UUID matchId ,int teamIndex){

        String key = "discussion:" + matchId + ":" + teamIndex;

        
        return playerDiscussionTemplate.opsForHash().values(key).stream().filter(PlayerDiscussionIdentification.class::isInstance).map(PlayerDiscussionIdentification.class::cast).toList();

    }

    public List<Long> getTeamScores(UUID matchId){

        String key = "liveMatch:" + matchId + ":scores";
        return scoreTemplate.opsForHash().values(key).stream().filter(Long.class::isInstance).map(Long.class::cast).toList();

        //return ((Long)scoreTemplate.opsForHash().get(key, Integer.toString(teamIndex)));

    }

    public void updatePlayerTeam(UUID matchId, Long playerId, int newTeam) {
        String key = "liveMatch:" + matchId + ":players";

        PlayerMatchDTO player = (PlayerMatchDTO) playerTemplate.opsForHash()
                .get(key, playerId.toString());

        if (player == null) {
            throw new IllegalArgumentException(" Player does not exist in cache");
            //return; // or throw
        }

        player.setPlayer_team(newTeam);

        playerTemplate.opsForHash()
                .put(key, playerId.toString(), player);
    }

    //public void incrementScore(UUID matchId, int team, int delta) {
    //    String key = "liveMatch:" + matchId + ":score:team:" + team;
    //    scoreTemplate.opsForValue().increment(key, delta);
    //}
}
