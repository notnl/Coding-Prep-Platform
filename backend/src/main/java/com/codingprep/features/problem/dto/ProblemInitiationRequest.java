package com.codingprep.features.problem.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;

@Data
public class ProblemInitiationRequest {

    @NotBlank(message = "Title cannot be blank.")
    @Size(max = 255, message = "Title cannot exceed 255 characters.")
    private String title;

    @NotBlank(message = "Slug cannot be blank.")
    @Size(max = 255, message = "Slug cannot exceed 255 characters.")
    @Pattern(regexp = "^[a-z0-9]+(?:-[a-z0-9]+)*$", message = "Slug must be URL-friendly (e.g., 'two-sum').")
    private String slug;

    @NotBlank(message = "Description cannot be blank.")
    private String description;

    @NotBlank(message = "Constraints cannot be blank.")
    private String constraints;

    @NotNull(message = "Points must be provided.")
    @Min(value = 1000, message = "Maximum difficulty must be at least 1000")
    @Max(value = 3500, message = "Maximum difficulty cannot exceed 3500")
    private Integer points;

    @NotNull(message = "Order to be displayed must be provided")
    private Integer order_for_tag;

    @NotNull(message = "Time limit must be provided.")
    @Min(value = 1, message = "Time limit must be at least 1 millisecond.")
    private Integer timeLimitMs;

    @NotNull(message = "Memory limit must be provided.")
    @Min(value = 1, message = "Memory limit must be at least 1 kilobyte.")
    private Integer memoryLimitKb;

    @Valid
    @NotNull(message = "Sample test cases cannot be null.")
    private JsonNode sampleTestCases;

    @Size(min = 1, message = "At least one tag is required.")
    private List<String> tags = new ArrayList<>();

    @Size(min = 1, message = "At least 1 input template must be given")
    private List<String> templateCode = new ArrayList<>();
}
/*
 * 
 *   "title": "Two Sum",
 *   "slug": "two-sum",
 *   "status": "PUBLISHED",
 *   "description": "
            Given an array of integers nums and an integer target,
            return indices of the two numbers such that they add up to target.

            You may assume that each input would have exactly one solution,
            and you may not use the same element twice.

            You can return the answer in any order.
            ",

      "constraints":"
            2 <= nums.length <= 10^5
            -10^9 <= nums[i] <= 10^9
            -10^9 <= target <= 10^9
            Exactly one valid answer exists.
            ",
        "points":100,
        "timeLimitMs": 1000,
        "memoryLimitKb": 262144,
        "sampleTestCases":  { 

              {
                "input": {
                  "nums": [2, 7, 11, 15],
                  "target": 9
                },
                "output": [0, 1],
                "explaination": ""
              },
              {
                "input": {
                  "nums": [3, 2, 4],
                  "target": 6
                },
                "output": [1, 2],
                "explaination": ""
              },
              {
                "input": {
                  "nums": [3, 3],
                  "target": 6
                },
                "output": [0, 1],

                "explaination": ""
              }

        }


 *
    Problem twoSumProblem = Problem.builder()
        .authorId(1L)
        .status(ProblemStatus.PUBLISHED)
        .slug("two-sum")
        .title("Two Sum")
        .id(uID)
        .description("""
            Given an array of integers nums and an integer target,
            return indices of the two numbers such that they add up to target.

            You may assume that each input would have exactly one solution,
            and you may not use the same element twice.

            You can return the answer in any order.
            """)
        .constraints("""
            2 <= nums.length <= 10^5
            -10^9 <= nums[i] <= 10^9
            -10^9 <= target <= 10^9
            Exactly one valid answer exists.
            """)
        .points(100)
        .timeLimitMs(1000)
        .memoryLimitKb(262144)
        .sampleTestCases("""
            [
              {
                "input": {
                  "nums": [2, 7, 11, 15],
                  "target": 9
                },
                "output": [0, 1],
                "explaination": ""
              },
              {
                "input": {
                  "nums": [3, 2, 4],
                  "target": 6
                },
                "output": [1, 2],
                "explaination": ""
              },
              {
                "input": {
                  "nums": [3, 3],
                  "target": 6
                },
                "output": [0, 1],

                "explaination": ""
              }
            ]
            """)
        .hiddenTestCasesS3Key("problems/two-sum/hidden-tests.json")
        .build();
 *
 *
 */
