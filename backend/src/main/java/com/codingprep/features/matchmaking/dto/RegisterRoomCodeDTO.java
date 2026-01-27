package com.codingprep.features.matchmaking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

import com.codingprep.features.auth.models.AuthenticationUser;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterRoomCodeDTO {
    private AuthenticationUser aU;
    private String accessToken;
}
