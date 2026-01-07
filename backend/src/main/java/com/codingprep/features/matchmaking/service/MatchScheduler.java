
package com.codingprep.features.matchmaking.service;

import com.codingprep.features.matchmaking.dto.CountdownStartPayload;
import com.codingprep.features.matchmaking.dto.LiveMatchStateDTO;
import com.codingprep.features.matchmaking.dto.PlayerMatchDTO;
import com.codingprep.features.matchmaking.models.MatchRoom;
import com.codingprep.features.matchmaking.models.MatchStatus;
import com.codingprep.features.matchmaking.models.PlayerDiscussionIdentification;
import com.codingprep.features.matchmaking.repository.LiveMatchStateRepository;
import com.codingprep.features.matchmaking.repository.MatchRepository;
import com.codingprep.features.redis.service.MatchRedisService;
import com.fasterxml.jackson.annotation.ObjectIdGenerators.UUIDGenerator;

//import com.codingprep.features.authentication.model.AuthenticationUser;
//import com.codingprep.features.authentication.repository.AuthenticationUserRepository;
//import com.codingprep.features.match.dto.LiveMatchStateDTO;
//import com.codingprep.features.match.model.Match;
//import com.codingprep.features.match.model.MatchStatus;
//import com.codingprep.features.match.repository.LiveMatchStateRepository;
//import com.codingprep.features.match.repository.MatchRepository;
//import com.codingprep.features.problem.repository.ProblemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MatchScheduler {

    private final MatchRepository matchRepository;
   // private final ProblemRepository problemRepository;
    private final LiveMatchStateRepository liveMatchStateRepository;
    private final MatchNotificationService matchNotificationService;
   // private final AuthenticationUserRepository userRepository;
   //
	private final MatchRedisService mR;

    private String getUsernameFromEmail(String email) {
        if (email == null || !email.contains("@")) {
            return "anonymous";
        }
        return email.substring(0, email.indexOf("@"));
    }

    private UUID randomU = UUID.randomUUID();

    @Scheduled(fixedRate = 15000)
    @Transactional
    public void startAllTimer() {

        List<MatchRoom> matchesToStart = matchRepository.findAllByStatus(
                MatchStatus.SCHEDULED
        );

        if (matchesToStart.isEmpty()) {
            System.out.println("No matches found");
            return;
        }

        for (MatchRoom match : matchesToStart) {

            //If the match is scheduled to run, we run it 
                if (Instant.now().getEpochSecond() > match.getScheduledAt().getEpochSecond()) {

                    
                    LiveMatchStateDTO lM = liveMatchStateRepository.findById(match.getId()).get();

                    if (lM == null) {  // if our lM does not exist in cache then we just delete
                                       //
                        System.out.println("lm"  + lM) ; 
                        matchRepository.delete(match); 
                        liveMatchStateRepository.delete(lM);
                        continue;
                    }

                    match.setStatus(MatchStatus.IN_PROGRESS);
                    match.setStartedAt(Instant.now());

                    lM.setMatchStatus(MatchStatus.IN_PROGRESS);
                    lM.setStartedAt(Instant.now());

                    System.out.println("Got here" + lM) ; 
                    matchRepository.save(match);

                    liveMatchStateRepository.save(lM);
                        

                    matchNotificationService.notifyMatchStart(
                            match.getId()
                    );
                }
            
        }

        //matchNotificationService.notifyCountdownStarted(uID, "LOBBY_COUNTDOWN_STARTED", matchPayload);
    }


    //@Scheduled(fixedRate = 15000)
    //@Transactional
    //public void startScheduledMatches() {
    //    log.info("Scheduler running: Looking for matches to start...");
    //    List<Match> matchesToStart = matchRepository.findAllByStatusAndScheduledAtBefore(
    //            MatchStatus.SCHEDULED,
    //            Instant.now()
    //    );

    //    if (matchesToStart.isEmpty()) {
    //        return;
    //    }

    //    for (Match match : matchesToStart) {
    //        log.info("Scheduler: Attempting to start match ID: {}", match.getId());

    //        try {
    //            Optional<UUID> problemIdOpt = problemRepository.findRandomUnsolvedProblemForTwoUsers(
    //                    match.getDifficultyMin(),
    //                    match.getDifficultyMax(),
    //                    match.getPlayerOneId(),
    //                    match.getPlayerTwoId()
    //            );

    //            if (problemIdOpt.isEmpty()) {
    //                log.warn("Could not find a suitable problem for match {}. Canceling match.", match.getId());
    //                match.setStatus(MatchStatus.CANCELED);
    //                match.setEndedAt(Instant.now());
    //                matchRepository.save(match);
    //                matchNotificationService.notifyMatchCanceled(match.getId(), "Could not find a suitable problem for both players.");
    //                continue;
    //            }

    //            UUID problemId = problemIdOpt.get();

    //            Map<Long, String> usernameMap = userRepository.findByIdIn(List.of(match.getPlayerOneId(), match.getPlayerTwoId())).stream()
    //                    .collect(Collectors.toMap(AuthenticationUser::getId, user -> getUsernameFromEmail(user.getEmail())));

    //            match.setStatus(MatchStatus.ACTIVE);
    //            match.setProblemId(problemId);
    //            match.setStartedAt(Instant.now());
    //            matchRepository.save(match);

    //            LiveMatchStateDTO liveState = LiveMatchStateDTO.builder()
    //                    .matchId(match.getId())
    //                    .problemId(problemId)
    //                    .playerOneId(match.getPlayerOneId())
    //                    .playerTwoId(match.getPlayerTwoId())
    //                    .startedAt(match.getStartedAt())
    //                    .durationInMinutes(match.getDurationInMinutes())
    //                    .build();

    //            long ttlInMinutes = match.getDurationInMinutes() + 1L;
    //            liveMatchStateRepository.save(liveState, ttlInMinutes);

    //            log.info("Successfully started match ID: {}. Live state created in Redis with TTL: {} minutes.", match.getId(), ttlInMinutes);

    //            matchNotificationService.notifyMatchStart(
    //                    match.getId(),
    //                    liveState,
    //                    usernameMap.get(match.getPlayerOneId()),
    //                    usernameMap.get(match.getPlayerTwoId())
    //            );

    //            long matchStartTime = match.getStartedAt().toEpochMilli();
    //            int matchDurationInSeconds = (int) match.getDurationInMinutes() * 60;
    //            CountdownStartPayload matchPayload = new CountdownStartPayload(matchStartTime, matchDurationInSeconds);
    //            matchNotificationService.notifyCountdownStarted(match.getId(), "MATCH_COUNTDOWN_STARTED", matchPayload);

    //        } catch (Exception e) {
    //            log.error("Scheduler: Unexpected error starting match ID: {}. Canceling.", match.getId(), e);
    //            match.setStatus(MatchStatus.CANCELED);
    //            match.setEndedAt(Instant.now());
    //            matchRepository.save(match);
    //            matchNotificationService.notifyMatchCanceled(match.getId(), "An internal error occurred while starting the match.");
    //        }
    //    }
    //}
}
