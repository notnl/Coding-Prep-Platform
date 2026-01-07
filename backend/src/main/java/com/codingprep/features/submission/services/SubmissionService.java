package com.codingprep.features.submission.services;


import com.codingprep.features.submission.dto.SubmissionDetailsDTO;
import com.codingprep.features.submission.dto.SubmissionRequest;
import com.codingprep.features.submission.models.Submission;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface SubmissionService {

    Submission createSubmission(SubmissionRequest request, Long userId);


    void processSubmission(UUID submissionId);


    Page<Submission> getSubmissionsForProblemAndUser(UUID problemId, Long userId, Pageable pageable);


    SubmissionDetailsDTO getSubmissionDetails(UUID submissionId);
    List<SubmissionDetailsDTO> getAllSubmissionByMatch(UUID matchId, Long userId);
}
