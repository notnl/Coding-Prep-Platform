package com.codingprep.features.submission.events;

import com.codingprep.features.AWS.service.SqsService;
import com.codingprep.features.submission.repository.SubmissionRepository;

import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

@Component
public class SubmissionEventListener {

    private final SqsService sqsService;
    private static final Logger logger = LoggerFactory.getLogger(SubmissionEventListener.class);
 //   private final SubmissionRepository submissionRepository;

    public SubmissionEventListener(SqsService sqsService) {
        this.sqsService = sqsService;
    }

//https://dev.to/haraf/understanding-transactioneventlistener-in-spring-boot-use-cases-real-time-examples-and-4aof
//
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onSubmissionCreated(SubmissionCreatedEvent event) {
        UUID submissionId = event.getSubmissionId();
        String logPrefix = "[POST_COMMIT_SQS submissionId=" + submissionId + "]";
        logger.info("{} Transaction committed. Now sending message to SQS.", logPrefix);
        try {
            sqsService.sendSubmissionMessage(submissionId);
            logger.info("{} Message sent successfully.", logPrefix);
        } catch (Exception e) {
            logger.error("{} CRITICAL: Failed to send SQS message post-commit.", logPrefix, e);
        }
    }
}
