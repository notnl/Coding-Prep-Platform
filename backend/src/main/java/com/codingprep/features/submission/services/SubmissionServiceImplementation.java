package com.codingprep.features.submission.services;

import com.codingprep.features.judge0.service.Judge0Service;
import com.codingprep.features.matchmaking.dto.LiveMatchStateDTO;
import com.codingprep.features.matchmaking.dto.PlayerMatchDTO;
import com.codingprep.features.matchmaking.models.DiscussionDetails;
import com.codingprep.features.matchmaking.models.DiscussionDetailsKey;
//import com.codingprep.features.AWS.service.S3Service;
//import com.codingprep.features.exception.InvalidRequestException;
//import com.codingprep.features.judge0.service.Judge0Service;
import com.codingprep.features.matchmaking.models.MatchRoom;
import com.codingprep.features.matchmaking.models.MatchStatus;
import com.codingprep.features.matchmaking.models.PlayerDiscussionIdentification;
import com.codingprep.features.matchmaking.repository.LiveMatchStateRepository;
import com.codingprep.features.matchmaking.repository.MatchRepository;
import com.codingprep.features.matchmaking.service.MatchNotificationService;
import com.codingprep.features.matchmaking.service.MatchService;
import com.codingprep.features.notificaton.service.NotificationService;
import com.codingprep.features.problem.dto.SampleTestCaseDTO;
import com.codingprep.features.problem.models.Problem;
import com.codingprep.features.problem.models.ProblemStatus;
import com.codingprep.features.problem.repository.ProblemRepository;
import com.codingprep.features.redis.service.MatchRedisService;
import com.codingprep.features.submission.dto.SubmissionDetailsDTO;
import com.codingprep.features.submission.dto.SubmissionRequest;
import com.codingprep.features.submission.dto.SubmissionResultDTO;
import com.codingprep.features.submission.events.SubmissionCreatedEvent;
import com.codingprep.features.submission.models.Language;
import com.codingprep.features.submission.models.Submission;
import com.codingprep.features.submission.models.SubmissionStatus;
import com.codingprep.features.submission.repository.SubmissionRepository;
import com.fasterxml.jackson.core.Base64Variant;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.micrometer.core.instrument.LongTaskTimer.Sample;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;


@Service
@RequiredArgsConstructor
public class SubmissionServiceImplementation implements SubmissionService {

//
//

//
//
    //private final S3Service s3Service;
    private final MatchService matchService;
    private final ObjectMapper objectMapper;
    private final Judge0Service judge0Service;
    private final MatchRepository matchRepository;
    private final LiveMatchStateRepository liveMatchStateRepository;
    private final ProblemRepository problemRepository;
    private final NotificationService notificationService;
    private final ApplicationEventPublisher eventPublisher;
    private final SubmissionRepository submissionRepository;
    private final MatchNotificationService matchNotificationService;
    private final RedisTemplate redisTemplate;
    private final MatchRedisService matchRedisService;

    private static final Logger logger = LoggerFactory.getLogger(SubmissionServiceImplementation.class);

    @Override
   @Transactional  
    public Submission createSubmission(SubmissionRequest request, Long userId) {
        
        try { 

        // 1. Validate match exists and is active
        LiveMatchStateDTO match = liveMatchStateRepository.findById(request.getMatchId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Match not found with ID: " + request.getMatchId()));
        
        if (match.getMatchStatus() != MatchStatus.IN_PROGRESS) {
            throw new IllegalArgumentException("Submissions are only allowed for active matches.");
        }
        
        // 2. Validate user is in match
        UUID problemUUID = UUID.fromString(request.getProblemId());
        PlayerMatchDTO curUser = matchRedisService.getAllPlayers(match.getMatchId()).stream()
                .filter(player -> player.getPlayer_id().equals(userId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("User does not exist in this match"));
        
        // 3. Create and save submission FIRST
        Submission submission = Submission.builder()
                .userId(userId)
                .matchId(request.getMatchId())
                .problemId(problemUUID)
                .code(request.getCode())
                .language(Language.fromSlug(request.getLanguage()))
                .status(SubmissionStatus.PENDING)
                .team_id(curUser.getPlayer_team())
                .build();
        
        Submission savedSubmission = submissionRepository.save(submission);
        logger.info("Submission saved with ID: {}, Match ID: {}", 
            savedSubmission.getId(), savedSubmission.getMatchId());
        
        // 4. Publish event WITHIN transaction
        eventPublisher.publishEvent(new SubmissionCreatedEvent(this, savedSubmission.getId()));
        
        // 5. Update Redis (outside transaction, handle failures)
        try {
            matchRedisService.updatePlayerDiscussion(
                match.getMatchId(), 
                userId,
                curUser.getPlayer_username(),
                curUser.getPlayer_team(), 
                request.getCode()
            );
            logger.info("Redis cache updated successfully");
        } catch (Exception e) {
            logger.error("Failed to update Redis cache, but submission was saved. Submission ID: {}", 
                savedSubmission.getId(), e);
                    throw e;
        }

        
        return savedSubmission;
        }
        catch ( Exception e ) { 
            logger.error("Error creating submission", e);
             throw e;  
        }
    }


    @Override
    @Transactional
    public void processSubmission(UUID submissionId) {
        String logPrefix = "[PROCESS_SUBMISSION id=" + submissionId + "]";
        logger.info("{} -> Starting processing workflow.", logPrefix);

        Submission submission = submissionRepository.findById(submissionId).orElse(null);


        if (submission == null) {
            logger.error("{} CRITICAL: No submission found. Aborting.", logPrefix);
            return;
        }


        UUID matchId = submission.getMatchId();



        logger.info("{} STEP A: Acquired submission. Setting status to PROCESSING.", logPrefix);
        submission.setStatus(SubmissionStatus.PROCESSING);
        submission = submissionRepository.save(submission);


        boolean alreadySolvedProblem = false;

        List<Submission> allSub = submissionRepository.retrieveAcceptedSolutionsByUser(submission.getUserId());
        for (Submission cSub : allSub){

            if (cSub.getMatchId().equals(matchId) && cSub.getProblemId().equals(submission.getProblemId())){ 
                alreadySolvedProblem = true;
                break;
            }
        }

        LiveMatchStateDTO match = liveMatchStateRepository.findById(matchId).orElseThrow(() -> new IllegalArgumentException("Match not found with ID: " + matchId));


        try {
            logger.info("{} STEP B: Fetching associated problem data.", logPrefix);
            Submission finalSubmission = submission;

            Problem problem = problemRepository.findById(finalSubmission.getProblemId())
                    .orElseThrow(() -> new IllegalStateException(
                            "Problem not found for ID: " + finalSubmission.getProblemId()));

            logger.info("{}   - Fetched problem '{}' (ID: {}).", logPrefix, problem.getTitle(), problem.getId());

            logger.info("{} STEP C: Gathering all test cases.", logPrefix);
            
            List<SampleTestCaseDTO> allSampleTestcases = objectMapper.readValue( problem.getSampleTestCases(), new TypeReference<List<SampleTestCaseDTO>>(){});

            logger.info("Sample Testcases {}", allSampleTestcases);

            List<Judge0Service.TestCase> allTestCases = new ArrayList<>();
            for (SampleTestCaseDTO sampleTestCaseDTO : allSampleTestcases) {
                
                Judge0Service.TestCase newT = new Judge0Service.TestCase(sampleTestCaseDTO.getInput(),sampleTestCaseDTO.getOutput());
                allTestCases.add(newT);
            }
            
            //List<Judge0Service.TestCase> allTestCases = s3Service.getOrFetchAllTestCases(problem);

            if (allTestCases.isEmpty()) {
                throw new IllegalStateException("No test cases (sample or hidden) found for problemId: " + problem.getId());
            }

            logger.info("{}   - Total test cases to be executed: {}.", logPrefix, allTestCases.size());

            logger.info("{} Final parsed testcases:", logPrefix);
            for (Judge0Service.TestCase tc : allTestCases) {
                logger.info("{}   Input='{}' | Expected='{}'", logPrefix, tc.input(), tc.expectedOutput());
            }

            if (submission.getCode() == null || submission.getCode().isBlank()) {
                throw new IllegalStateException("Submission code is empty for submissionId: " + submission.getId());
            }

            String fullCode = submission.getCode();
            logger.info("{}   - USER CODE LENGTH: {}, FULL CODE LENGTH: {}.", logPrefix, submission.getCode().length(), fullCode.length());

            logger.info("{} STEP E: Sending code to Judge0 for execution.", logPrefix);
            SubmissionResultDTO tempResult = judge0Service.executeCode(fullCode, submission.getLanguage().getSlug(), allTestCases, matchId);
            logger.info("{}   - Execution complete. Status: {}, Runtime: {}ms, Memory: {}KB", logPrefix,
                    tempResult.getStatus(), tempResult.getRuntimeMs(), tempResult.getMemoryKb());

            logger.info("{} STEP F: Persisting final result to the database.", logPrefix);
            submission.setStatus(tempResult.getStatus());
            submission.setRuntimeMs(tempResult.getRuntimeMs());
            submission.setMatchId(matchId);
            submission.setMemoryKb(tempResult.getMemoryKb());
            submission.setStderr(tempResult.getStderr());
            submission.setStdout(tempResult.getStdout());

            //Before we persist the submission, check if we already solved the problem
            //Check if user already solved the problem, this is not efficient but will make do for now
            //this is for increasing team score purposes, we still want users to be able to submit 
            //despite already scoring the points for their team 
            
            
            Submission savedSubmission = submissionRepository.save(submission);

            logger.info("{} STEP G: Sending WebSocket notification.", logPrefix);
            SubmissionResultDTO finalResult = SubmissionResultDTO.fromEntity(savedSubmission);





            //If the result is successful
            if (finalResult.getStatus() == SubmissionStatus.ACCEPTED) {
                for (PlayerMatchDTO d : matchRedisService.getAllPlayers(matchId)){

                    if (submission.getUserId().equals( d.player_id) && !alreadySolvedProblem) {

                        logger.info("Increase Team score : {}", problem.getPoints().longValue());
                        matchRedisService.increaseTeamScore(matchId, d.player_team, problem.getPoints().longValue()); // Just set a static score for now , there is no

                        matchNotificationService.notifyPointsUpdate(matchId);
                        break;

                    }
                }

            }

            //if (matchId != null) {
            //    logger.info("[PROCESS_SUBMISSION id={}] This submission belongs to duel {}. Notifying MatchService.", submissionId, matchId);
            //    try {
            //        matchService.processDuelSubmissionResult(
            //                matchId,
            //                savedSubmission.getUserId(),
            //                savedSubmission.getStatus()
            //        );
            //    } catch (Exception e) {
            //        logger.error("[PROCESS_SUBMISSION id={}] CRITICAL: Failed to process duel state update for match {}.", submissionId, matchId, e);
            //    }
            //}

            notificationService.notifyUser(submission.getUserId(), submission.getId(), finalResult);
            logger.info("{} <- Processing workflow completed successfully.", logPrefix);

        } catch (Exception e) {
            logger.error("{} CRITICAL: An uncaught exception occurred. Updating submission to INTERNAL_ERROR.", logPrefix, e);
            //handleProcessingError(submission, e, logPrefix);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Submission> getSubmissionsForProblemAndUser(UUID problemId, Long userId, Pageable pageable) {
        return submissionRepository.findByProblemIdAndUserIdOrderByCreatedAtDesc(problemId, userId, pageable);
    }


    ////private void handleProcessingError(Submission submission, Exception e, String logPrefix) {
    //    logger.debug("{} Entering error handling block.", logPrefix);
    //    try {
    //        submission.setStatus(SubmissionStatus.INTERNAL_ERROR);
    //        submission.setStderr("Something went wrong");
    //        submission.setMatchId(submission.getMatchId());
    //        Submission savedSubmission = submissionRepository.save(submission);
    //        logger.info("{}   - Submission status updated to INTERNAL_ERROR in database.", logPrefix);
    //        logger.debug("{}   - Sending error notification.", logPrefix);
    //        SubmissionResultDTO errorResult = SubmissionResultDTO.fromEntity(savedSubmission);
    //        notificationService.notifyUser(submission.getUserId(), submission.getId(), errorResult);
    //        logger.info("{} <- Error handling complete.", logPrefix);
    //    } catch (Exception handlerEx) {
    //        logger.error("{} CATASTROPHIC: Failed to even save the INTERNAL_ERROR state for submission {}. Final error: {}", logPrefix, submission.getId(), handlerEx.getMessage(), handlerEx);
    //    }
    //}


    private String extractStd64(String passStr){
        String collectStr = "";

        if (passStr == null || passStr == "") {
            return collectStr;
        }

        Stream<String> curStream = passStr.lines();

        try { 

        Iterator<String> it = curStream.iterator();
        while (it.hasNext()) {
            String s = it.next();
            byte[] decoded = Base64.getDecoder().decode(s);
            collectStr += new String(decoded, StandardCharsets.UTF_8) + "\n";
        }
            
        } catch (Exception e){

        }
        return collectStr;

    }

    @Override
    public List<SubmissionDetailsDTO> getAllSubmissionByMatch(UUID matchId, Long userId){

        return submissionRepository
        .findByUserIdAndMatchId(userId, matchId)
        .stream()
        .map(submission -> {

            //IS inefficient but for now will just use this 

        return SubmissionDetailsDTO.builder()
                .id(submission.getId())
                .matchId(submission.getMatchId())
                .status(submission.getStatus())
                .language(submission.getLanguage().name())
                .code(submission.getCode())
                .runtimeMs(submission.getRuntimeMs())
                .memoryKb(submission.getMemoryKb())
                .stdout(submission.getStdout())
                .stderr(submission.getStderr())
                .createdAt(submission.getCreatedAt())
                .build();
        })
        .toList();

    }
    @Override
    public SubmissionDetailsDTO getSubmissionDetails(UUID submissionId) {
        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new EntityNotFoundException("Submission not found with id: " + submissionId));

        Problem problem = problemRepository.findById(submission.getProblemId())
                .orElseThrow(() -> new EntityNotFoundException("Problem not found with id: " + submission.getProblemId()));
        
        
        //String errorStr = extractStd64(submission.getStderr());
        //String outStr = extractStd64(submission.getStdout());


        return SubmissionDetailsDTO.builder()
                .id(submission.getId())
                .problemId(problem.getId())
                .matchId(submission.getMatchId())
                .problemTitle(problem.getTitle())
                .problemSlug(problem.getSlug())
                .status(submission.getStatus())
                .language(submission.getLanguage().name())
                .code(submission.getCode())
                .runtimeMs(submission.getRuntimeMs())
                .memoryKb(submission.getMemoryKb())
                .stdout(submission.getStdout())
                .stderr(submission.getStderr())
                .createdAt(submission.getCreatedAt())
                .build();
    }
}
