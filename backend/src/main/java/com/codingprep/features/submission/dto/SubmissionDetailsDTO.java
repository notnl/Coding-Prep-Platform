package com.codingprep.features.submission.dto;

import com.codingprep.features.submission.models.SubmissionStatus;
import lombok.Builder;
import lombok.Data;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class SubmissionDetailsDTO {
    private UUID id;
    private UUID problemId;
    private UUID matchId;
    private String problemTitle;
    private String problemSlug;
    private SubmissionStatus status;
    private String language;
    private String code;
    private Integer runtimeMs;
    private Integer memoryKb;
    private String stdout;
    private String stderr;
    private Instant createdAt;
}
