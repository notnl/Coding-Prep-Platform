package com.codingprep.features.auth.dto;

import java.util.UUID;

import lombok.Builder;


@Builder
public record UserStatusDTO (UUID in_match,int in_match_team) 
{

}
