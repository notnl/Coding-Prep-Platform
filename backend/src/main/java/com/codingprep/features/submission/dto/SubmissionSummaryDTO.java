package com.codingprep.features.submission.dto;


import com.codingprep.features.submission.models.Language;
import com.codingprep.features.submission.models.Submission;
import com.codingprep.features.submission.models.SubmissionStatus;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class SubmissionSummaryDTO {
    private UUID id;
    private UUID matchId;
    private SubmissionStatus status;
    private Language language;
    private Integer runtimeMs;
    private Instant createdAt;

    public static SubmissionSummaryDTO fromEntity(Submission submission) {
        return SubmissionSummaryDTO.builder()
                .id(submission.getId())
                .matchId(submission.getMatchId())
                .status(submission.getStatus())
                .language(submission.getLanguage())
                .runtimeMs(submission.getRuntimeMs())
                .createdAt(submission.getCreatedAt())
                .build();
    }
}
