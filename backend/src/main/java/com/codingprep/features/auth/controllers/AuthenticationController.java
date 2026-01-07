package com.codingprep.features.auth.controllers;



import org.springframework.web.bind.annotation.*;

import com.codingprep.features.auth.dto.LoginRequest;
import com.codingprep.features.auth.dto.UserCredentials;
import com.codingprep.features.auth.dto.UserStatusDTO;
import com.codingprep.features.auth.models.AuthenticationUser;
import com.codingprep.features.auth.service.AuthenticationService;
import com.codingprep.features.matchmaking.models.MatchRoom;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.util.List;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;



@RestController
@RequestMapping("/api/v1/auth")
public class AuthenticationController{
    private final AuthenticationService authenticationService;

    public AuthenticationController(AuthenticationService aS){
        this.authenticationService = aS;
    }

    @PostMapping("/login")
        public ResponseEntity<UserCredentials> loginRequest(@RequestBody LoginRequest lR) {
             return ResponseEntity.ok(authenticationService.HandleLoginRequest(lR)); 

          
        }
        
    @PostMapping("/register")
        public ResponseEntity<String> registerRequest(@RequestBody LoginRequest lR) {
            return ResponseEntity.ok(authenticationService.handleRegisterRequest(lR)); 

        }

    @GetMapping("/status")
        public ResponseEntity<UserStatusDTO> getUserStatus(@AuthenticationPrincipal AuthenticationUser authUser) {
            return ResponseEntity.ok(authenticationService.handleUserStatus(authUser.getId())); 
        }
        

}
