package com.codingprep.features.matchmaking.dto;


import lombok.Getter;

@Getter
public class CountdownStartPayload {

    private final long startTime;

    public CountdownStartPayload(long startTime) {
        this.startTime = startTime;
    }

}
