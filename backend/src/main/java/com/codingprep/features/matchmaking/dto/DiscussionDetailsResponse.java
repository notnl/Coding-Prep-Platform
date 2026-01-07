package com.codingprep.features.matchmaking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

import com.codingprep.features.matchmaking.models.PlayerDiscussionIdentification;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DiscussionDetailsResponse {

    public List<PlayerDiscussionIdentification> allCode;
}
