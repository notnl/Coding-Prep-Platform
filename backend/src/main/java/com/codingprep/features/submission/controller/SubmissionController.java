package com.codingprep.features.submission.controller;


import com.codingprep.features.auth.models.AuthenticationUser;
import com.codingprep.features.submission.dto.*;
import com.codingprep.features.submission.models.Submission;
import com.codingprep.features.submission.services.SubmissionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal; // <-- IMPORTANT IMPORT
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/submissions")
@RequiredArgsConstructor
public class SubmissionController {

    private final SubmissionService submissionService;


    @PostMapping("/create")
    public ResponseEntity<SubmissionResponse> createSubmission(
            @Valid @RequestBody SubmissionRequest request,
            @AuthenticationPrincipal AuthenticationUser user) {

        
        Submission submission = submissionService.createSubmission(request, user.getId());

        SubmissionResponse responseBody = new SubmissionResponse(submission.getId());

        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .body(responseBody);
    }


    @GetMapping("/problem/{problemId}")
    public ResponseEntity<PaginatedSubmissionResponse> getSubmissions(
            @PathVariable UUID problemId,
            Pageable pageable,
            @AuthenticationPrincipal AuthenticationUser user) {

        Page<Submission> submissionPage = submissionService.getSubmissionsForProblemAndUser(problemId, user.getId(), pageable);

        PaginatedSubmissionResponse response = PaginatedSubmissionResponse.builder()
                .submissions(submissionPage.getContent().stream().map(SubmissionSummaryDTO::fromEntity).toList())
                .currentPage(submissionPage.getNumber())
                .totalPages(submissionPage.getTotalPages())
                .totalItems(submissionPage.getTotalElements())
                .build();

        return ResponseEntity.ok(response);
    }


    @GetMapping("/{submissionId}")
    public ResponseEntity<SubmissionDetailsDTO> getSubmissionById(
            @PathVariable UUID submissionId) {
        SubmissionDetailsDTO submissionDetails = submissionService.getSubmissionDetails(submissionId);
        return ResponseEntity.ok(submissionDetails);
    }

    @GetMapping("/get/{matchId}")
    public ResponseEntity<List<SubmissionDetailsDTO>> getAllSubmissionByMatch(
            @PathVariable UUID matchId,@AuthenticationPrincipal AuthenticationUser user) {

        List<SubmissionDetailsDTO> submissionDetails = submissionService.getAllSubmissionByMatch(matchId,user.getId());
        return ResponseEntity.ok(submissionDetails);
    }



}
