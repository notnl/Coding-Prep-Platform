package com.codingprep.features.matchmaking.service;

import com.codingprep.features.auth.dto.UserStatusDTO;
import com.codingprep.features.auth.models.UserStatus;
import com.codingprep.features.auth.repository.UserRepository;
import com.codingprep.features.auth.repository.UserStatusRepository;
//import com.codingprep.features.authentication.model.AuthenticationUser;
//import com.codingprep.features.authentication.repository.AuthenticationUserRepository;
//import com.codingprep.features.exception.InvalidRequestException;
//import com.codingprep.features.exception.MatchAlreadyCompletedException;
//import com.codingprep.features.exception.ResourceConflictException;
//import com.codingprep.features.exception.ResourceNotFoundException;
import com.codingprep.features.matchmaking.dto.*;
import com.codingprep.features.matchmaking.models.DiscussionDetails;
import com.codingprep.features.matchmaking.models.DiscussionDetailsKey;
import com.codingprep.features.matchmaking.models.MatchRoom;
import com.codingprep.features.matchmaking.models.MatchStatus;
import com.codingprep.features.matchmaking.models.PlayerDiscussionIdentification;
import com.codingprep.features.matchmaking.repository.LiveMatchStateRepository;
//import com.codingprep.features.match.model.UserStats;
//import com.codingprep.features.match.repository.LiveMatchStateRepository;
import com.codingprep.features.matchmaking.repository.MatchRepository;
//import com.fasterxml.jackson.databind.deser.DataFormatReaders.Match;
//import com.codingprep.features.match.repository.UserStatsRepository;
import com.codingprep.features.problem.dto.ProblemDetailResponse;
import com.codingprep.features.problem.models.Problem;
import com.codingprep.features.problem.models.ProblemStatus;
import com.codingprep.features.problem.repository.ProblemRepository;
import com.codingprep.features.redis.service.MatchRedisService;
import com.codingprep.features.submission.models.SubmissionStatus;

//import com.codingprep.features.submission.model.Submission;
//import com.codingprep.features.submission.model.SubmissionStatus;
//import com.codingprep.features.submission.repository.SubmissionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
//import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.codingprep.features.matchmaking.dto.CreateMatchRequest;
import com.codingprep.features.matchmaking.dto.CreateMatchResponse;
import com.codingprep.features.matchmaking.dto.JoinMatchRequest;
import com.codingprep.features.matchmaking.dto.JoinMatchResponse;

import java.io.Console;
import java.nio.charset.Charset;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.xml.stream.events.Characters;

import org.springframework.cache.annotation.Cacheable;



@Service
@RequiredArgsConstructor
@Slf4j
public class MatchServiceImplementation implements MatchService {

    private final MatchRepository matchRepository;
    private final LiveMatchStateRepository liveMatchStateRepository;
    private final ProblemRepository problemRepository;
    private final UserStatusRepository userStatusRepository;
    private final MatchNotificationService matchNotificationService;
    private final MatchRedisService matchRedisService;


    public static final long PENALTY_MINUTES = 5;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    private List<PlayerMatchDTO> getAllPlayersFromCache(UUID matchId){
        return matchRedisService.getAllPlayers(matchId);
    }

    private int findUserTeam(LiveMatchStateDTO match,long userId) { 

        int userTeamIndex  = -1;

        for (PlayerMatchDTO d : getAllPlayersFromCache(match.getMatchId())) {
            if (d.player_id == userId) {
                userTeamIndex = d.player_team;
                return userTeamIndex;
            }

        }

        return userTeamIndex;

    }

    private String getUsernameFromEmail(String email) {
        if (email == null || !email.contains("@")) {
            return "anonymous";
        }
        return email.substring(0, email.indexOf("@"));
    }

    private MatchRoom checkMatchState(Optional<MatchRoom> mR) {

        if (mR.isEmpty()) {
            throw new IllegalArgumentException("Match does not exist");
        }

        MatchRoom curMatch = mR.get();
        if (curMatch.getStatus() == MatchStatus.COMPLETED) {
            throw new IllegalArgumentException("Match is already completed.");
        }
        return curMatch;
    }

    private void updatePlayerCollection(Collection<PlayerMatchDTO> curList, PlayerMatchDTO curPlayer) { 

    }


    @Override
    public List<Long> getTeamPoints(UUID matchId){
        List<Long> retPoints = matchRedisService.getTeamScores(matchId);
        System.out.println(retPoints);
        return retPoints;
    }

    @Override
    @Transactional
    public CreateMatchResponse createMatch(CreateMatchRequest request, Long creatorId, String creatorUsername) {
        if (request.getDifficultyMin() > request.getDifficultyMax()) {
            throw new IllegalArgumentException("Minimum difficulty cannot be greater than maximum difficulty.");
        }
        //String roomCode = RandomStringUtils.randomAlphanumeric(6).toUpperCase();
        //

        //6 Characters worth of roomCode
        //byte[] bArray = new byte[6]; 
        //new Random().nextBytes(bArray);


        String roomCode = request.getRoomCode();

        List<PlayerMatchDTO> newCol = new ArrayList<PlayerMatchDTO>();
        PlayerMatchDTO builtPlayer = PlayerMatchDTO.builder().player_id(creatorId).player_username(creatorUsername).player_team(0).build();
        newCol.add(PlayerMatchDTO.builder().player_id(creatorId).player_username(creatorUsername).player_team(0).build());


        MatchRoom match = MatchRoom.builder()
                .roomCode(roomCode)
                .host_id(creatorId)
                .max_player_count(64)
                .status(MatchStatus.IN_LOBBY)
                .startDelayInSecond(request.getStartDelayInSecond())
                .durationInMinutes(request.getDurationInMinutes())
                .build();

        MatchRoom savedMatch = matchRepository.save(match); // store so we can get the UUID
                                                            //


        List<UUID> allProblems = new ArrayList<UUID>(); // Assume every problem is for the match
                                                                 // 
        for (Problem p : problemRepository.findAll()){
            allProblems.add(p.getId());
        }

        
        //Map<Integer,Integer> allTeamScore = new HashMap<Integer,Integer>();

        //for (int i = 0; i < 4;++i){
        //    allTeamScore.put(i, 0);
        //}

        //Cache initializations
            LiveMatchStateDTO lM = LiveMatchStateDTO.builder().matchId(savedMatch.getId())
                .roomCode(roomCode)
                 .hostId(creatorId).max_player_count(64)
                 .startDelayInSecond(request.getStartDelayInSecond())
                .matchStatus(MatchStatus.IN_LOBBY).durationInMinutes(match.getDurationInMinutes()).startedAt(match.getStartedAt())
                .currentProblem(0).maxProblemCount(allProblems.size()).allProblems(allProblems) // Assume 1 count
                .build();




        liveMatchStateRepository.save(lM);  // Save to cache
                                            //
                                            
        matchRedisService.addPlayer(savedMatch.getId(), builtPlayer); // Save player list to cache

        for (int i = 0; i < 4;++i){ // Create 4 keys for our player discusison repository for each team
            matchRedisService.createTeamScore(savedMatch.getId(), i, 0L);

        }
//      Optional<DiscussionDetails> pD = playerDiscussionRepository.findById(savedMatch.getId());



//       PlayerDiscussionIdentification setValue =  
//           PlayerDiscussionIdentification.builder().playerName(curUser.getPlayer_username()).playerId(curUser.getPlayer_id()).playerCode(request.getCode()).build();
//
//        if (pD.isEmpty()) { //Create if empty 
//            
//            Map<Long,PlayerDiscussionIdentification> newMap = new HashMap<Long,PlayerDiscussionIdentification>();
//            newMap.put(userId, setValue);
//            //playerDiscussionRepository.save(DiscussionDetails.builder().matchId(request.getMatchId()).teamIndex(curUser.getPlayer_team()).playerDiscussionList(newMap).build());
//            //
//            //DiscussionDetailsKey dK = DiscussionDetailsKey.builder().matchId(request.getMatchId()).teamIndex(curUser.getPlayer_team()).build();
//            playerDiscussionRepository.save(DiscussionDetails.builder().id(builtId).matchId(request.getMatchId()).teamIndex(curUser.getPlayer_team()).playerDiscussionList(newMap).build());
//
        


        
        return new CreateMatchResponse(savedMatch.getId(), roomCode);
    }


    @Override
    @Transactional
    public JoinMatchResponse joinMatch(JoinMatchRequest request, Long joiningUserId, String joiningUserName) {

        LiveMatchStateDTO match = liveMatchStateRepository.findById(request.getMatchId()).orElseThrow
            ( () -> new IllegalArgumentException("Match room not found with ID: " + request.getMatchId()))
             ;
                //.orElseThrow(() -> new IllegalArgumentException("Match room not found with code: " + request.getRoomCode()));

        if (match.getMatchStatus() != MatchStatus.IN_LOBBY) {
            throw new IllegalArgumentException("This match is not in its lobby stage, can't join");
        }


        List<PlayerMatchDTO> allP = getAllPlayersFromCache(match.getMatchId());

        if (allP.size() >= match.getMax_player_count()) {
            throw new IllegalArgumentException("Lobby is full");
        }

        for (PlayerMatchDTO t : allP){

            if (t.player_id == joiningUserId) { 


                return new JoinMatchResponse(match.getMatchId()); //Handle rejoin
            }
        
        }



        matchRedisService.addPlayer(match.getMatchId(), PlayerMatchDTO.builder().player_id(joiningUserId).player_username(joiningUserName).player_team(0).build()); // Add player to the match

        liveMatchStateRepository.save(match);

        matchNotificationService.notifyPlayerJoined(match.getMatchId(),"PLAYER_JOINED");

        UserStatus curUserStatus = userStatusRepository.findById(joiningUserId).orElseThrow();
        curUserStatus.setIn_match(match.getMatchId());
        userStatusRepository.save(curUserStatus);

        return new JoinMatchResponse(match.getMatchId());
    }

    @Override
    @Transactional(readOnly = true)
    public LobbyStateDTO getLobbyState(UUID matchId) {



        LiveMatchStateDTO curMatch = liveMatchStateRepository.findById(matchId).orElseThrow();
        List<PlayerMatchDTO> allPlayers = matchRedisService.getAllPlayers(matchId);
        
        return LobbyStateDTO.builder().allPlayers(allPlayers).durationInMinutes(curMatch.getDurationInMinutes()).maxPlayers(curMatch.getMax_player_count()).hostId(curMatch.getHostId()).matchId(curMatch.getMatchId()).status(curMatch.getMatchStatus()).build();

    }

    @Override
    @Transactional
    public void startLobbyCountdown(UUID matchId) {

        MatchRoom match = matchRepository.findById(matchId).orElseThrow(() -> new IllegalArgumentException("Match not found with id: " + matchId));

        //For now just update cache stuff
        LiveMatchStateDTO curMatch = liveMatchStateRepository.findById(matchId).orElseThrow
            ( () -> new IllegalArgumentException("Match room not found with ID: " + matchId))
             ;
        
        //.orElseThrow(() -> new ResourceNotFoundException("Match not found with id: " + matchId));
        //
        //System.out.println("Start Lobby countdown for : " + match);
        //if (match.isEmpty()){ 

        //        System.out.println("Empty match" );
        //    throw new IllegalArgumentException("Match not found with id: " + matchId);
        //}
        
        //MatchRoom curMatch =  match.get();

        match.setStatus(MatchStatus.SCHEDULED);
        curMatch.setMatchStatus(MatchStatus.SCHEDULED);
        //curMatch.setCreatedAt(Instant.now());
        Instant scheduledTime = Instant.now().plus(curMatch.getStartDelayInSecond(), ChronoUnit.SECONDS);
        curMatch.setScheduledAt(scheduledTime);
        match.setScheduledAt(scheduledTime);

        liveMatchStateRepository.save(curMatch);
        matchRepository.save(match);


        CountdownStartPayload lobbyPayload = new CountdownStartPayload(scheduledTime.toEpochMilli());
        matchNotificationService.notifyCountdownStarted(curMatch.getMatchId(), "LOBBY_COUNTDOWN_STARTED", lobbyPayload);


    }
    @Override

    @Transactional(readOnly = true)
    public MatchStateResponseDTO getMatchState(UUID matchId) {

        //Optional<MatchRoom> match = matchRepository.findById(matchId);

        //MatchRoom curMatch = matchRepository.findById(matchId).orElseThrow(() -> new IllegalArgumentException("Match not found with id: " + matchId));
                //.orElseThrow(() -> new ResourceNotFoundException("Match not found with id: " + matchId));

        //if (match.isEmpty()) {
        //    throw new IllegalArgumentException("Match " + matchId + " is already completed.");
        //}

        //MatchRoom curMatch = match.get();
        //if (curMatch.getStatus() == MatchStatus.COMPLETED) {
        //    throw new IllegalArgumentException("Match " + matchId + " is already completed.");
        //}

        LiveMatchStateDTO curMatch = liveMatchStateRepository.findById(matchId).orElseThrow(() -> new IllegalArgumentException("Active match not found in cache for match ID: " + matchId));


        //UUID uID = new UUID(1,0);

        ////Problem problemEntity = problemRepository.findById(liveState.getProblemId())
                //.orElseThrow(() -> new ResourceNotFoundException("Problem not found for ID: " + liveState.getProblemId()));


        ArrayList<ProblemDetailResponse> pDTOList = new ArrayList<ProblemDetailResponse>();  
        
//        ArrayList<ProblemDetailResponse> pDTOList = new ArrayList<ProblemDetailResponse>(); 
//        
        for (UUID uID : curMatch.getAllProblems()) {

            Problem problemEntity = problemRepository.findById(uID).get(); // For now find 0 )
            ProblemDetailResponse problemDTO = ProblemDetailResponse.fromEntity(problemEntity);
            pDTOList.add(problemDTO);
        }

        //
        //ProblemDetailResponse problemDTO = ProblemDetailResponse.fromEntity(twoSumProblem);

        
        

        return MatchStateResponseDTO.builder()
                .matchId(curMatch.getMatchId())
                .durationInMinutes(curMatch.getDurationInMinutes())
                .startedAt(curMatch.getStartedAt())
                .matchStatus(curMatch.getMatchStatus())
                .hostId(curMatch.getHostId())
                .problemDetails(pDTOList)
                .teamScores(matchRedisService.getTeamScores(matchId))
                .currentProblem(curMatch.getCurrentProblem())
                .build();
    }



    @Override
    @Transactional
    public  List<LobbyDetailsDTO> getAllLobby(){

        Iterable<LiveMatchStateDTO> match = liveMatchStateRepository.findAll();

         List<LobbyDetailsDTO> target = new ArrayList<>();



        for (LiveMatchStateDTO l : match) {

                if (l != null && l.getMatchStatus() == MatchStatus.IN_LOBBY){

                        target.add(LobbyDetailsDTO.builder().matchId(l.getMatchId()).roomCode(l.getRoomCode()).build());
                }
        }

        return target;

    }

    @Override
    @Transactional
   public void hostStartDiscussion(UUID matchId, Long userId){

        LiveMatchStateDTO match = liveMatchStateRepository.findById(matchId).orElseThrow();
        if (!match.getHostId().equals(userId)) { 
            throw new IllegalArgumentException("Not host");
        }

        System.out.println("Host start discussion" + userId + " For match : " + match) ;
        
        // Toggle start and end discussion
        if (match.getMatchStatus() == MatchStatus.IN_PROGRESS) { 
            match.setMatchStatus(MatchStatus.DISCUSSION);
            matchNotificationService.notifyMatchUpdate(matchId,"MATCH_DISCUSSION");

        }else if (match.getMatchStatus() == MatchStatus.DISCUSSION){
            match.setMatchStatus(MatchStatus.IN_PROGRESS);
            matchNotificationService.notifyMatchUpdate(matchId,"MATCH_PROGRESS");
        }

        liveMatchStateRepository.save(match);

   }

    @Override

    @Transactional
   public void hostEndMatch(UUID matchId, Long userId){

        LiveMatchStateDTO match = liveMatchStateRepository.findById(matchId).orElseThrow();
        if (match.getHostId() != userId) { 
            throw new IllegalArgumentException("Not host");
        }


        MatchRoom curMatch = matchRepository.findById(matchId).orElseThrow(() -> new IllegalArgumentException("Match not found with id: " + matchId));

        curMatch.setStatus(MatchStatus.COMPLETED);
        matchRepository.save(curMatch);

        matchNotificationService.notifyMatchUpdate(matchId,"MATCH_END");

        liveMatchStateRepository.delete(match); //Can remove from cache
                                                //
                                               
        List<PlayerMatchDTO> alP =matchRedisService.getAllPlayers(matchId);
        //UserStatusDTO
        //userStatusRepository.save(entity)
        for (PlayerMatchDTO playerMatchDTO : alP) {

        UserStatus curUserStatus = userStatusRepository.findById(playerMatchDTO.getPlayer_id()).orElseThrow();

        curUserStatus.setIn_match(null);
        userStatusRepository.save(curUserStatus);
        }


    //public List<PlayerMatchDTO> getAllPlayers(UUID matchId) {

    //    String key = PlayerKeyString(matchId);

    //    return playerTemplate.opsForHash().values(key).stream().filter(PlayerMatchDTO.class::isInstance).map(
    //            PlayerMatchDTO.class::cast).toList();

    //}
        

   }

    @Override

    @Transactional
   public int hostNextProblem(UUID matchId, Long userId){

        LiveMatchStateDTO match = liveMatchStateRepository.findById(matchId).orElseThrow();
        if (match.getHostId() != userId) { 
            throw new IllegalArgumentException("Not host");
        }
        if (match.getCurrentProblem() == null){
            match.setCurrentProblem(0);
            match.setMaxProblemCount(2);
        }

        int nextProblem = match.getCurrentProblem() + 1 ;
        if (nextProblem  >= match.getMaxProblemCount() ) {
            throw new IllegalArgumentException("Last problem reached");
        }
        match.setCurrentProblem(nextProblem);

        liveMatchStateRepository.save(match);

        matchNotificationService.notifyMatchUpdate(matchId,"MATCH_NEXTPROBLEM");
        return nextProblem;

        
   }

    @Override
    @Transactional
    public void switchTeamMatch(UUID matchId, Long userId,int toTeam){


        LiveMatchStateDTO match = liveMatchStateRepository.findById(matchId).orElseThrow();

        if (match.getMatchStatus() != MatchStatus.IN_LOBBY) {
            throw new IllegalArgumentException("NOT IN LOBBY");
        }

        matchRedisService.updatePlayerTeam(matchId, userId, toTeam);

        //for (PlayerMatchDTO d : getAllPlayersFromCache(matchId)) {
        //    if (d.player_id == userId) {
        //        d.setPlayer_team(toTeam); 
        //    }

        //}

        //liveMatchStateRepository.save(match);


        matchNotificationService.notifyPlayerJoined(matchId,"PLAYER_JOINED"); //To refresh for all users
        
    }

    @Override
    @Transactional(readOnly = true)
    public DiscussionDetailsResponse getTeamDiscussionCode(UUID matchId,long userId){

        int userTeamIndex  = -1;
        for (PlayerMatchDTO d : getAllPlayersFromCache(matchId)) {
            if (d.player_id.equals(userId)) {
                userTeamIndex = d.player_team;
                break;
            }

        }

        if (userTeamIndex < 0 ){ 
            throw new IllegalArgumentException("Uesr does not exist?");
        }

       

            

        //List<PlayerDiscussionIdentification> allPD = new ArrayList<PlayerDiscussionIdentification>();
        //
        List<PlayerDiscussionIdentification> allPD = matchRedisService.getAllPlayerDiscussion(matchId, userTeamIndex);

        //System.out.println(allPD);
        //for (var entry : pD.getPlayerDiscussionList().entrySet()) {

        //    System.out.println(entry);
        //   allPD.add(entry.getValue());
        //}

        
        return DiscussionDetailsResponse.builder().allCode(allPD).build();
    }


    @Override
    public void processSubmissionResult(UUID matchId, Long userId, SubmissionStatus submissionStatus) {
        String logPrefix = String.format("[SUBMISSION_PROCESS matchId=%s userId=%d]", matchId, userId);
        log.info("{} Received submission result with status: {}", logPrefix, submissionStatus);
        //LiveMatchStateDTO liveState = liveMatchStateRepository.findById(matchId).orElse(null);

        //if (liveState == null) {
        //    log.warn("{} Could not find live state in Redis. Match might have already completed or timed out.", logPrefix);
        //    return;
        //}
        //if (submissionStatus.equals(SubmissionStatus.ACCEPTED)) {
        //    log.info("{} Submission was ACCEPTED. Triggering 'sudden death' match completion.", logPrefix);
        //    if (userId.equals(liveState.getPlayerOneId()) && liveState.getPlayerOneFinishTime() == null) {
        //        liveState.setPlayerOneFinishTime(Instant.now());
        //    } else if (userId.equals(liveState.getPlayerTwoId()) && liveState.getPlayerTwoFinishTime() == null) {
        //        liveState.setPlayerTwoFinishTime(Instant.now());
        //    }
        //    liveMatchStateRepository.save(liveState, null);
        //    this.completeMatch(matchId);
        //} else {
        //    log.info("{} Submission was not accepted. Updating penalties.", logPrefix);
        //    if (userId.equals(liveState.getPlayerOneId())) {
        //        liveState.setPlayerOnePenalties(liveState.getPlayerOnePenalties() + 1);
        //    } else if (userId.equals(liveState.getPlayerTwoId())) {
        //        liveState.setPlayerTwoPenalties(liveState.getPlayerTwoPenalties() + 1);
        //    }
        //    liveMatchStateRepository.save(liveState, null);

        //    matchNotificationService.notifyMatchUpdate(matchId, liveState);
        //    log.info("{} Penalties updated in Redis and notification sent.", logPrefix);
        //}
    }


    //@Override
    //@Transactional
    //public void completeMatch(UUID matchId) {
    //    String logPrefix = String.format("[MATCH_COMPLETION matchId=%s]", matchId);
    //    log.info("{} Starting match completion process...", logPrefix);
    //    Match match = matchRepository.findById(matchId)
    //            .orElseThrow(() -> new ResourceNotFoundException("Match not found in database with ID: " + matchId));

    //    if (match.getStatus() == MatchStatus.COMPLETED) {
    //        log.warn("{} Match is already completed. Aborting.", logPrefix);
    //        return;
    //    }
    //    Optional<LiveMatchStateDTO> liveStateOpt = liveMatchStateRepository.findById(matchId);


    //    if (liveStateOpt.isEmpty()) {
    //        log.warn("{} Live match state not found in Redis. Assuming timeout completion.", logPrefix);
    //        match.setStatus(MatchStatus.COMPLETED);
    //        match.setEndedAt(Instant.now());
    //        MatchResultDTO results = this.buildMatchResults(match);
    //        matchNotificationService.notifyMatchEnd(matchId, results);
    //        matchRepository.save(match);
    //        updateUserStatsForDraw(match.getPlayerOneId(), match.getPlayerTwoId());
    //        return;
    //    }

    //    LiveMatchStateDTO liveState = liveStateOpt.get();

    //    Long p1Id = liveState.getPlayerOneId();
    //    Long p2Id = liveState.getPlayerTwoId();
    //    Instant startTime = liveState.getStartedAt();
    //    Instant p1FinishTime = liveState.getPlayerOneFinishTime();
    //    Instant p2FinishTime = liveState.getPlayerTwoFinishTime();
    //    Duration p1EffectiveTime = (p1FinishTime != null) ? Duration.between(startTime, p1FinishTime).plusMinutes(liveState.getPlayerOnePenalties() * PENALTY_MINUTES) : null;
    //    Duration p2EffectiveTime = (p2FinishTime != null) ? Duration.between(startTime, p2FinishTime).plusMinutes(liveState.getPlayerTwoPenalties() * PENALTY_MINUTES) : null;
    //    Long winnerId = null;
    //    boolean isDraw = false;
    //    if (p1EffectiveTime != null && (p2EffectiveTime == null || p1EffectiveTime.compareTo(p2EffectiveTime) < 0)) { winnerId = p1Id; } else if (p2EffectiveTime != null && (p1EffectiveTime == null || p2EffectiveTime.compareTo(p1EffectiveTime) < 0)) { winnerId = p2Id; } else if (p1EffectiveTime != null && p1EffectiveTime.equals(p2EffectiveTime)) { isDraw = true; } else { isDraw = true; }
    //    log.info("{} Winner determined. WinnerID: {}, isDraw: {}", logPrefix, winnerId, isDraw);
    //    match.setStatus(MatchStatus.COMPLETED);
    //    match.setEndedAt(Instant.now());
    //    match.setWinnerId(winnerId);
    //    match.setPlayerOnePenalties(liveState.getPlayerOnePenalties());
    //    match.setPlayerTwoPenalties(liveState.getPlayerTwoPenalties());
    //    match.setPlayerOneFinishTime(liveState.getPlayerOneFinishTime());
    //    match.setPlayerTwoFinishTime(liveState.getPlayerTwoFinishTime());
    //    matchRepository.save(match);
    //    log.info("{} Match entity updated to COMPLETED in database with final results.", logPrefix);
    //    updateUserStats(p1Id, p2Id, winnerId, isDraw);
    //    log.info("{} User stats updated for both players.", logPrefix);

    //    MatchResultDTO results = this.buildMatchResults(match);

    //    liveMatchStateRepository.deleteById(matchId);
    //    log.info("{} Live state for match removed from Redis.", logPrefix);

    //    matchNotificationService.notifyMatchEnd(matchId, results);

    //    log.info("{} <- Match completion process finished successfully.", logPrefix);
    //}


    //private void updateUserStats(Long p1Id, Long p2Id, Long winnerId, boolean isDraw) {
    //    Map<Long, UserStats> statsMap = userStatsRepository.findAllById(Arrays.asList(p1Id, p2Id)).stream().collect(Collectors.toMap(UserStats::getUserId, Function.identity()));
    //    UserStats p1Stats = statsMap.computeIfAbsent(p1Id, id -> { UserStats newUserStats = new UserStats(); newUserStats.setUserId(id); return newUserStats; });
    //    UserStats p2Stats = statsMap.computeIfAbsent(p2Id, id -> { UserStats newUserStats = new UserStats(); newUserStats.setUserId(id); return newUserStats; });

    //    p1Stats.setDuelsPlayed(p1Stats.getDuelsPlayed() + 1);
    //    p2Stats.setDuelsPlayed(p2Stats.getDuelsPlayed() + 1);

    //    if (isDraw) {
    //        p1Stats.setDuelsDrawn(p1Stats.getDuelsDrawn() + 1);
    //        p2Stats.setDuelsDrawn(p2Stats.getDuelsDrawn() + 1);
    //    } else {
    //        if (winnerId.equals(p1Id)) {
    //            p1Stats.setDuelsWon(p1Stats.getDuelsWon() + 1);
    //            p2Stats.setDuelsLost(p2Stats.getDuelsLost() + 1);
    //        } else {
    //            p2Stats.setDuelsWon(p2Stats.getDuelsWon() + 1);
    //            p1Stats.setDuelsLost(p1Stats.getDuelsLost() + 1);
    //        }
    //    }
    //    userStatsRepository.saveAll(Arrays.asList(p1Stats, p2Stats));

    //    List<AuthenticationUser> users = userRepository.findAllById(Arrays.asList(p1Id, p2Id));
    //    for (AuthenticationUser user : users) {
    //        String username = getUsernameFromEmail(user.getEmail());
    //        log.info("Evicting profile from cache for username: {}", username);
    //        Objects.requireNonNull(cacheManager.getCache("userProfiles")).evict(username);
    //    }
    //}


    //private void updateUserStatsForDraw(Long p1Id, Long p2Id) {
    //    if (p1Id == null || p2Id == null) return;
    //    updateUserStats(p1Id, p2Id, null, true);
    //}


    //@Override
    //@Transactional(readOnly = true)
    //public MatchResultDTO getMatchResults(UUID matchId) {
    //    Match match = matchRepository.findById(matchId)
    //            .orElseThrow(() -> new ResourceNotFoundException("Match not found with id: " + matchId));
    //    if (match.getStatus() != MatchStatus.COMPLETED) {
    //        throw new InvalidRequestException("Match results are not available until the match is completed.");
    //    }
    //    return buildMatchResults(match);
    //}


    //private MatchResultDTO buildMatchResults(Match match) {
    //    List<Submission> allSubmissions = submissionRepository.findByMatchIdOrderByCreatedAtAsc(match.getId());
    //    PlayerResultDTO playerOneResult = buildPlayerResultFromStored(match.getPlayerOneId(), match, allSubmissions);
    //    PlayerResultDTO playerTwoResult = buildPlayerResultFromStored(match.getPlayerTwoId(), match, allSubmissions);

    //    String outcome;
    //    String winnerUsername = null;
    //    Long winnerId = match.getWinnerId();

    //    if (winnerId == null) {
    //        outcome = "DRAW";
    //    } else {
    //        winnerUsername = userRepository.findById(winnerId)
    //                .map(user -> this.getUsernameFromEmail(user.getEmail()))
    //                .orElse("Unknown Player");

    //        if (winnerId.equals(match.getPlayerOneId())) {
    //            outcome = "PLAYER_ONE_WIN";
    //        } else {
    //            outcome = "PLAYER_TWO_WIN";
    //        }
    //    }

    //    UUID winningSubmissionId = null;
    //    if (winnerId != null) {
    //        // Use the repository method to find the winner's first accepted submission
    //        List<Submission> winnerSubmissions = submissionRepository
    //                .findByMatchIdAndUserIdAndStatusOrderByCreatedAtAsc(
    //                        match.getId(),
    //                        winnerId,
    //                        SubmissionStatus.ACCEPTED
    //                );

    //        if (!winnerSubmissions.isEmpty()) {
    //            winningSubmissionId = winnerSubmissions.get(0).getId();
    //        }
    //    }

    //    return MatchResultDTO.builder()
    //            .matchId(match.getId())
    //            .problemId(match.getProblemId())
    //            .startedAt(match.getStartedAt())
    //            .endedAt(match.getEndedAt())
    //            .winnerId(match.getWinnerId())
    //            .winnerUsername(winnerUsername)
    //            .outcome(outcome)
    //            .playerOne(playerOneResult)
    //            .playerTwo(playerTwoResult)
    //            .winningSubmissionId(winningSubmissionId)
    //            .build();
    //}


    //private PlayerResultDTO buildPlayerResultFromStored(Long userId, Match match, List<Submission> allSubmissions) {
    //    if (userId == null) return null;
    //    Instant finishTime;
    //    int penalties;
    //    if (userId.equals(match.getPlayerOneId())) {
    //        finishTime = match.getPlayerOneFinishTime();
    //        penalties = match.getPlayerOnePenalties();
    //    } else {
    //        finishTime = match.getPlayerTwoFinishTime();
    //        penalties = match.getPlayerTwoPenalties();
    //    }
    //    Duration effectiveTime = null;
    //    if (finishTime != null && match.getStartedAt() != null) {
    //        Duration rawDuration = Duration.between(match.getStartedAt(), finishTime);
    //        effectiveTime = rawDuration.plus(penalties * PENALTY_MINUTES, ChronoUnit.MINUTES);
    //    }
    //    List<SubmissionTimelineDTO> timeline = allSubmissions.stream()
    //            .filter(s -> s.getUserId().equals(userId))
    //            .map(SubmissionTimelineDTO::fromEntity)
    //            .collect(Collectors.toList());
    //    return PlayerResultDTO.builder()
    //            .userId(userId)
    //            .solved(finishTime != null)
    //            .finishTime(finishTime)
    //            .penalties(penalties)
    //            .effectiveTime(effectiveTime)
    //            .submissions(timeline)
    //            .build();
    //}


    //private static final Set<MatchStatus> PAST_MATCH_STATUSES = EnumSet.of(
    //        MatchStatus.COMPLETED,
    //        MatchStatus.CANCELED,
    //        MatchStatus.EXPIRED
    //);


    //@Override
    //@Transactional(readOnly = true)
    //@Cacheable(value = "matchHistory")
    //public PageDto<PastMatchDto> getPastMatchesForUser(Long userId, String result, Pageable pageable) {
    //    Page<Match> matchesPage;

    //    if ("WIN".equalsIgnoreCase(result)) {
    //        matchesPage = matchRepository.findUserWins(userId, PAST_MATCH_STATUSES, pageable);
    //    } else if ("LOSS".equalsIgnoreCase(result)) {
    //        matchesPage = matchRepository.findUserLosses(userId, PAST_MATCH_STATUSES, pageable);
    //    } else if ("DRAW".equalsIgnoreCase(result)) {
    //        matchesPage = matchRepository.findUserDraws(userId, PAST_MATCH_STATUSES, pageable);
    //    } else {
    //        matchesPage = matchRepository.findUserMatchesByStatus(userId, PAST_MATCH_STATUSES, pageable);
    //    }

    //    Page<PastMatchDto> dtoPage = matchesPage.map(match -> convertToDto(match, userId));

    //    return new PageDto<>(dtoPage);
    //}




    //private PastMatchDto convertToDto(Match match, Long currentUserId) {
    //    Long opponentId = Objects.equals(match.getPlayerOneId(), currentUserId)
    //            ? match.getPlayerTwoId()
    //            : match.getPlayerOneId();

    //    String opponentUsername = "Unknown";
    //    if (opponentId != null) {
    //        opponentUsername = userRepository.findById(opponentId)
    //                .map(user -> getUsernameFromEmail(user.getEmail()))
    //                .orElse("Unknown");
    //    }

    //    String problemTitle = "Unknown Problem";
    //    if (match.getProblemId() != null) {
    //        problemTitle = problemRepository.findById(match.getProblemId())
    //                .map(Problem::getTitle)
    //                .orElse("Unknown Problem");
    //    }

    //    return PastMatchDto.builder()
    //            .matchId(match.getId())
    //            .status(match.getStatus())
    //            .result(determineResult(match, currentUserId))
    //            .opponentId(opponentId)
    //            .opponentUsername(opponentUsername)
    //            .problemId(match.getProblemId())
    //            .problemTitle(problemTitle)
    //            .endedAt(match.getEndedAt())
    //            .createdAt(match.getCreatedAt())
    //            .build();
    //}


    //private String determineResult(Match match, Long currentUserId) {
    //    switch (match.getStatus()) {
    //        case COMPLETED:
    //            if (match.getWinnerId() == null) { return "DRAW"; }
    //            return Objects.equals(match.getWinnerId(), currentUserId) ? "WIN" : "LOSS";
    //        case CANCELED:
    //            return "CANCELED";
    //        case EXPIRED:
    //            return "EXPIRED";
    //        default:
    //            return "UNKNOWN";
    //    }
    //}
}
