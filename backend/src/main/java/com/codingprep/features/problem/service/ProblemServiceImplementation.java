package com.codingprep.features.problem.service;

import java.util.HashSet;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.codingprep.features.auth.models.AuthenticationUser;
import com.codingprep.features.problem.dto.ProblemInitiationRequest;
import com.codingprep.features.problem.dto.ProblemInitiationResponse;
import com.codingprep.features.problem.models.Problem;
import com.codingprep.features.problem.models.ProblemStatus;
import com.codingprep.features.problem.models.Tag;
import com.codingprep.features.problem.repository.ProblemRepository;
import com.codingprep.features.problem.repository.TagRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProblemServiceImplementation implements ProblemService{  
                    
    
    private final ProblemRepository problemRepository;
    private final TagRepository tagRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public ProblemInitiationResponse initiateProblemCreation(ProblemInitiationRequest requestDto, AuthenticationUser user) {
        boolean isAdmin = user.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin) {
            throw new IllegalArgumentException("Not admin");
        }

        //if (!isAdmin) {
        //    TemporaryPermission permission = permissionRepository.findActiveCreatePermissionForUser(user.getId(), LocalDateTime.now())
        //            .orElseThrow(() -> new AuthorizationException("User does not have a valid permission to create a problem."));

        //    permission.setConsumed(true);
        //    permissionRepository.save(permission);
        //}

        //if (getTotalProblemCount().getTotalCount() >= problemLimit) {
        //    throw new ResourceConflictException(
        //            "Problem creation limit reached. Cannot create more than " + problemLimit + " problems."
        //    );
        //}

        problemRepository.findBySlug(requestDto.getSlug()).ifPresent(p -> {
            throw new IllegalArgumentException("Slug '" + requestDto.getSlug() + "' is already in use.");
        });

        Set<Tag> problemTags = new HashSet<>();
        for (String tagName : requestDto.getTags()) {
            Tag tag = tagRepository.findByName(tagName)
                    .orElseGet(() -> tagRepository.save(Tag.builder().name(tagName).build()));
            problemTags.add(tag);
        }

        Problem problem = new Problem();
        problem.setTitle(requestDto.getTitle());
        problem.setSlug(requestDto.getSlug());
        problem.setDescription(requestDto.getDescription());
        problem.setConstraints(requestDto.getConstraints());
        problem.setPoints(requestDto.getPoints());
        problem.setOrder_for_tag(requestDto.getOrder_for_tag());
        problem.setTimeLimitMs(requestDto.getTimeLimitMs());
        problem.setMemoryLimitKb(requestDto.getMemoryLimitKb());
        problem.setAuthorId(user.getId());
        problem.setTags(problemTags);

        problem.setStatus(ProblemStatus.PENDING_TEST_CASES);

        try {
            problem.setSampleTestCases(objectMapper.writeValueAsString(requestDto.getSampleTestCases()));
            problem.setTemplateCode(objectMapper.writeValueAsString(requestDto.getTemplateCode()));

            
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Internal error: Failed to serialize problem data.", e);
        }

        Problem savedProblem = problemRepository.save(problem);
        //UUID problemId = savedProblem.getId();
        //String uploadUrl;

        //try {
        //    String s3Key = "uploads/pending/" + problemId.toString() + "/testcases.zip";
        //    uploadUrl = s3Service.generatePresignedUploadUrl(s3Key);

        //    String redisKey = PENDING_PROBLEM_KEY_PREFIX + problemId;
        //    redisTemplate.opsForValue().set(redisKey, "", 24, TimeUnit.HOURS);
        //    logger.info("Set Redis expiration key '{}' with a 24-hour TTL for problemId: {}", redisKey, problemId);
        //} catch (Exception e) {
        //    logger.error("Failed to generate pre-signed URL or set Redis key for problemId: {}", problemId, e);
        //    throw new ServiceUnavailableException("Could not initiate problem creation due to an external service error. Please try again later.", e);
        //}

        //logger.info("=======================================================");
        //logger.info("GENERATED PRE-SIGNED URL for problem {}: {}", problemId, uploadUrl);
        //logger.info("=======================================================");

        return new ProblemInitiationResponse(savedProblem.getId());
    }

}
