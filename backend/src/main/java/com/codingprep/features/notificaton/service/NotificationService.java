package com.codingprep.features.notificaton.service;

import com.codingprep.features.submission.dto.SubmissionResultDTO;

import java.util.UUID;

public interface NotificationService {
    void notifyUser(Long userId, UUID submissionId, SubmissionResultDTO result);
}
