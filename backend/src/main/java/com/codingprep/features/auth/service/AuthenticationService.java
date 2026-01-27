
package com.codingprep.features.auth.service;

import java.io.Console;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import org.springframework.security.authentication.ProviderManager;

import com.codingprep.features.auth.dto.LoginRequest;
import com.codingprep.features.auth.dto.UserCredentials;
import com.codingprep.features.auth.dto.UserStatusDTO;
import com.codingprep.features.auth.models.AuthenticationUser;
import com.codingprep.features.auth.models.Role;
import com.codingprep.features.auth.models.RoleType;
import com.codingprep.features.auth.models.UserStatus;
import com.codingprep.features.auth.repository.RoleRepository;
import com.codingprep.features.auth.repository.UserRepository;
import com.codingprep.features.auth.repository.UserStatusRepository;
import com.codingprep.features.auth.utils.PasswordValidator;
import com.codingprep.features.matchmaking.dto.JoinMatchRoomCodeResponse;
import com.codingprep.features.matchmaking.dto.RegisterRoomCodeDTO;

import org.springframework.security.crypto.password.PasswordEncoder;

import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    
    private final JwtService jwtService;

    private final UserRepository userRepository;
    private final UserStatusRepository userStatusRepository;
    private final PasswordEncoder passwordEncoder;

    private final AuthenticationManager authManager;
    private final RoleRepository roleRepository;

    @Value("${jwt.secret.key}")
    private String secret;

    private static final long ACCESS_TOKEN_EXPIRATION = 30 * 60 * 1000; // 30 min
    private static final long REFRESH_TOKEN_EXPIRATION = 7 * 24 * 60 * 60 * 1000; // 7 days
                                                                                  //
    public AuthenticationUser getUser(String username) {
        return userRepository.checkUserExists(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));
    }
                                                                                  //
    public UserCredentials HandleLoginRequest(LoginRequest lR) { 

        Authentication authenticationResponse = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(lR.username(),lR.password()));
        
        if (!authenticationResponse.isAuthenticated()){
            throw new IllegalArgumentException("Not authenticated properly");
        }

        AuthenticationUser user = getUser(lR.username());

        String aToken = jwtService.generateAccessToken(user);
        String rToken = jwtService.generateRefreshToken(user);
        UserCredentials uC = new UserCredentials(aToken,rToken);
        return uC;

    }

    public String handleRegisterRequest(LoginRequest lR) { 

            // Find in database see if username exists 
        if (!userRepository.checkUserExists(lR.username()).isEmpty()){

            System.out.println("User Exist");
            throw new IllegalArgumentException("User already exists");
        }

        if (!PasswordValidator.isValid(lR.password())) {

            throw new IllegalArgumentException("Password must be at least 8 characters long and include uppercase, lowercase, number, and special character.");
        }
          
        
        AuthenticationUser aU = new AuthenticationUser(lR.username(),passwordEncoder.encode(lR.password()));



        Role userRole = roleRepository.findByName(RoleType.ROLE_USER)
                .orElseThrow(() -> new IllegalStateException("ROLE_USER not found in database."));
        aU.setRoles(new HashSet<>(Set.of(userRole)));



            // if not we add to database
        userRepository.save(aU);
        UserStatus uS = new UserStatus();
        uS.setUser(aU);
        userStatusRepository.save(uS);

        return "User registered!";

    }
    
    public RegisterRoomCodeDTO handleRegisterRequestByRoomCode(LoginRequest lR) { 


            // Find in database see if username exists 
        if (!userRepository.checkUserExists(lR.username()).isEmpty()){

            System.out.println("User Exist");
            throw new IllegalArgumentException("User already exists");
        }

        if (!PasswordValidator.isValid(lR.password())) {

            throw new IllegalArgumentException("Password must be at least 8 characters long and include uppercase, lowercase, number, and special character.");
        }
          
        
        AuthenticationUser aU = new AuthenticationUser(lR.username(),passwordEncoder.encode(lR.password()));



        Role userRole = roleRepository.findByName(RoleType.ROLE_USER)
                .orElseThrow(() -> new IllegalStateException("ROLE_USER not found in database."));
        aU.setRoles(new HashSet<>(Set.of(userRole)));



            // if not we add to database
        userRepository.save(aU);
        UserStatus uS = new UserStatus();
        uS.setUser(aU);
        userStatusRepository.save(uS);

        String aToken = jwtService.generateAccessToken(aU);

        return RegisterRoomCodeDTO.builder().accessToken(aToken).aU(aU).build();


    }
 
    public UserStatusDTO handleUserStatus(Long uId) { 
        UserStatus uS = userStatusRepository.findById(uId).orElseThrow();

        return UserStatusDTO.builder().in_match(uS.getIn_match()).in_match_team(uS.getIn_match_team()).build();

    }


}
