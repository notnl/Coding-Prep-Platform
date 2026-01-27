package com.codingprep.features.redis.service;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.codingprep.features.matchmaking.dto.PlayerMatchDTO;
import com.codingprep.features.matchmaking.models.PlayerDiscussionIdentification;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MatchRedisService {

    private final RedisTemplate<String, PlayerMatchDTO> playerTemplate;

    private final RedisTemplate<String, Long> scoreTemplate;

    private final RedisTemplate<String, PlayerDiscussionIdentification> playerDiscussionTemplate;


    private String PlayerKeyString(UUID matchId){
        return "liveMatch:" + matchId + ":players";
    }

    private String PlayerCountKeyString(UUID matchId){
        return "liveMatch:" + matchId + ":count";
    }

    private String ScoreKeyString(UUID matchId){
        return "liveMatch:" + matchId + ":scores";
    }


    private String MatchNamingKeyString(UUID matchId){
        return "liveMatch:" + matchId + ":nameIndex";
    }
    private String TeamDiscussionKeyString(UUID matchId,int teamIndex){
        return "discussion:" + matchId + ":" + teamIndex;
    }


    public void addPlayer(UUID matchId, PlayerMatchDTO player) {

        String key = PlayerKeyString(matchId);
        playerTemplate.opsForHash()
                .put(key, player.getPlayer_id().toString(), player);
    }

    public void removePlayer(UUID matchId, PlayerMatchDTO player) {

        String key = PlayerKeyString(matchId);
        playerTemplate.opsForHash()
                .delete(key, player.getPlayer_id().toString(), player);
    }
    public List<PlayerMatchDTO> getAllPlayers(UUID matchId) {

        String key = PlayerKeyString(matchId);

        return playerTemplate.opsForHash().values(key).stream().filter(PlayerMatchDTO.class::isInstance).map(
                PlayerMatchDTO.class::cast).toList();

    }
    public boolean checkPlayerExist(UUID matchId, Long player_id) {

        String key = PlayerKeyString(matchId);
        return scoreTemplate.opsForHash().get(key,player_id.toString()) != null;

    }
    public void createTeamScore(UUID matchId, int teamIndex,Long byScore) { 

        String key = ScoreKeyString(matchId);
        scoreTemplate.opsForHash().put(key, Integer.toString(teamIndex), byScore);
        scoreTemplate.expire(key, Duration.ofHours(6L)); // After a set time just get rid of the score, don't really expect a live session to last longer than that

    }
    public boolean tryReserveSlot(UUID matchId, int maxPlayers) {
        // Redis INCR + check + DECR if over
        String key = PlayerCountKeyString(matchId);
        Long newCount = scoreTemplate.opsForValue()
            .increment(key);

        if (newCount > maxPlayers) {
            scoreTemplate.opsForValue().decrement(key);
            return false;
        }
        return true;

    }

    public Long addToMatchNamingIndex(UUID matchId, Long byScore) {
        String key = MatchNamingKeyString(matchId);

        Long curValue = scoreTemplate.opsForValue().increment(key, byScore);
        

        // Optionally ensure expiry is set (see note below)
        scoreTemplate.expire(key, Duration.ofHours(6L));
        return curValue;
    }

    public void increaseTeamScore(UUID matchId, int teamIndex,Long byScore){

        String key = ScoreKeyString(matchId);

        scoreTemplate.opsForHash().increment(key, Integer.toString(teamIndex), byScore);

    }

    public List<Long> getTeamScores(UUID matchId){

        String key = ScoreKeyString(matchId);
        return scoreTemplate.opsForHash().values(key).stream().filter(Long.class::isInstance).map(Long.class::cast).toList();

        //return ((Long)scoreTemplate.opsForHash().get(key, Integer.toString(teamIndex)));

    }

    public void updatePlayerDiscussion(UUID matchId,Long userId,String playerName, int teamIndex,String code){

        String key = TeamDiscussionKeyString(matchId, teamIndex);

        
        PlayerDiscussionIdentification pD = PlayerDiscussionIdentification.builder().playerCode(code).playerId(userId).playerName(playerName).build();

        playerDiscussionTemplate.opsForHash().put(key,Long.toString(userId),pD);

    }

    public List<PlayerDiscussionIdentification> getAllPlayerDiscussion(UUID matchId ,int teamIndex){

        String key = TeamDiscussionKeyString(matchId, teamIndex);
        
        return playerDiscussionTemplate.opsForHash().values(key).stream().filter(PlayerDiscussionIdentification.class::isInstance).map(PlayerDiscussionIdentification.class::cast).toList();

    }


    public void updatePlayerTeam(UUID matchId, Long playerId, int newTeam) {
        String key = PlayerKeyString(matchId);


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

}
