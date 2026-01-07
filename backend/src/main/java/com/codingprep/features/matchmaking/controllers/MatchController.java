package com.codingprep.features.matchmaking.controllers;

import com.codingprep.features.auth.models.AuthenticationUser;
import com.codingprep.features.matchmaking.dto.*;
import com.codingprep.features.matchmaking.service.MatchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/match")
@RequiredArgsConstructor
public class MatchController {

    private final MatchService matchService;


    @PostMapping("/create")
    public ResponseEntity<CreateMatchResponse> createRoom(
            @Valid @RequestBody CreateMatchRequest request,
            @AuthenticationPrincipal AuthenticationUser user) {

        CreateMatchResponse response = matchService.createMatch(request, user.getId(),user.getUsername());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/join")
    public ResponseEntity<JoinMatchResponse> joinRoom(
             @RequestBody JoinMatchRequest request,
            @AuthenticationPrincipal AuthenticationUser user) {
        System.out.println("Join req : " + request);
        System.out.println(SecurityContextHolder.getContext().getAuthentication().getPrincipal().getClass());
        
        JoinMatchResponse jR = matchService.joinMatch(request, user.getId(),user.getUsername()); // Will throw error if fail
                                                                                                 //
        System.out.println("Join response : " + jR);
        return ResponseEntity.ok(jR);
    }

    @GetMapping("/alllobby")
    public ResponseEntity<List<LobbyDetailsDTO>> getAllLobby() {
        List<LobbyDetailsDTO> lobbyState = matchService.getAllLobby();
        return ResponseEntity.ok(lobbyState);
    }

    @GetMapping("/lobby/{matchId}")
    public ResponseEntity<LobbyStateDTO> getLobbyState(@PathVariable UUID matchId) {
        LobbyStateDTO lobbyState = matchService.getLobbyState(matchId);
        return ResponseEntity.ok(lobbyState);
    }

    @PostMapping("/lobby/{matchId}/start")
    public ResponseEntity<StartCountDownResponse> startLobby(@PathVariable UUID matchId) {
        matchService.startLobbyCountdown(matchId);
        StartCountDownResponse resp = new StartCountDownResponse();
        resp.setMessage("success");
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/lobby/{matchId}/switch")
    public ResponseEntity<String> switchTeamMatch(@PathVariable UUID matchId,@RequestBody SwitchTeamRequest request,@AuthenticationPrincipal AuthenticationUser user) {
        matchService.switchTeamMatch(matchId,user.getId(),request.getToTeam());                                           
        return ResponseEntity.ok("Success");
    }

    @GetMapping("/{matchId}")
    public ResponseEntity<MatchStateResponseDTO> getMatchState(@PathVariable UUID matchId) {
        MatchStateResponseDTO machState = matchService.getMatchState(matchId);
        return ResponseEntity.ok(machState);
    }


    @PostMapping("/{matchId}/startdiscussion")
    public ResponseEntity<Void> startTeamDiscussion(@PathVariable UUID matchId,@AuthenticationPrincipal AuthenticationUser user) {
        matchService.hostStartDiscussion(matchId,user.getId());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{matchId}/nextproblem")
    public ResponseEntity<Integer> startNextProblem(@PathVariable UUID matchId,@AuthenticationPrincipal AuthenticationUser user) {
        int nextProblem = matchService.hostNextProblem(matchId,user.getId());
        return ResponseEntity.ok(nextProblem);
    }

    @PostMapping("/{matchId}/end")
    public ResponseEntity<Void> endMatch(@PathVariable UUID matchId,@AuthenticationPrincipal AuthenticationUser user) {
         matchService.hostEndMatch(matchId,user.getId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{matchId}/discussion")
    public ResponseEntity<DiscussionDetailsResponse> getTeamDiscussionCode(@PathVariable UUID matchId,@AuthenticationPrincipal AuthenticationUser user) {
        
        DiscussionDetailsResponse resp = matchService.getTeamDiscussionCode(matchId,user.getId());
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/{matchId}/points")
    public ResponseEntity<List<Long>> getTeamPoints(@PathVariable UUID matchId,@AuthenticationPrincipal AuthenticationUser user) {
        
        return ResponseEntity.ok(matchService.getTeamPoints(matchId));
    }

    //@GetMapping("/{matchId}/results")
    //public ResponseEntity<MatchResultDTO> getMatchResults(@PathVariable UUID matchId) {
    //    MatchResultDTO results = matchService.getMatchResults(matchId);
    //    return ResponseEntity.ok(results);
    //}



    //@GetMapping("/history")
    //public ResponseEntity<PageDto<PastMatchDto>> getMatchHistory(
    //        @AuthenticationPrincipal AuthenticationUser user,
    //        @PageableDefault(size = 10) Pageable pageable,
    //        @RequestParam(required = false) String result) {

    //    PageDto<PastMatchDto> pastMatches = matchService.getPastMatchesForUser(user.getId(), result, pageable);

    //    return ResponseEntity.ok(pastMatches);
    //}

}
