
package com.codingprep.features.auth.models;



import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Entity(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AuthenticationUser implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;

    @Email
    private String email;

    private Boolean emailVerified = false;

    private String emailVerificationToken; // Check this tokenAuthent
    private LocalDateTime emailVerificationTokenExpiryDate;

    @JsonIgnore
    private String password;

    private String passwordResetToken; // Need check this token
    private LocalDateTime passwordResetTokenExpiryDate;

    private Boolean twoFactorEnabled = false;
    private String twoFactorToken;
    private boolean twoFactorTokenRequested = false;
    private LocalDateTime twoFactorTokenExpiryDate;

    private String refreshToken;


    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();



    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    @PrimaryKeyJoinColumn
    private UserStatus status;   
    


    public AuthenticationUser(String username, String password) {
        this.username = username;
        this.password = password;
    }


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority(role.getName().name()))
                .collect(Collectors.toList());
    }


    @Override
    public String getUsername() {
        return this.username;
    }

    public String getEmail(){
        return this.email;
    }

    @Override
    public boolean isAccountNonExpired() { return true; }


    @Override
    public boolean isAccountNonLocked() { return true; }


    @Override
    public boolean isCredentialsNonExpired() { return true; }


    @Override
    public boolean isEnabled() { return true; }


    public Long getId() {return id;}
}
