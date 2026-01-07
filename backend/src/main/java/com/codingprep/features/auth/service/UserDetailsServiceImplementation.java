
package com.codingprep.features.auth.service;

import com.codingprep.features.auth.models.AuthenticationUser;
import com.codingprep.features.auth.repository.UserRepository;


import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;


@Service
public class UserDetailsServiceImplementation implements UserDetailsService {

    private final UserRepository repository;
    public UserDetailsServiceImplementation(UserRepository repository) {
        this.repository = repository; 
    }

    @Override
    public AuthenticationUser loadUserByUsername(String username) throws UsernameNotFoundException {

        return repository.checkUserExists(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }
}
