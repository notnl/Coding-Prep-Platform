
package com.codingprep.features.auth.models;


import java.util.UUID;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Entity
@Table(name = "user_status")
@Getter
@Setter
@NoArgsConstructor
public class UserStatus {

    

    @Id
    @Column(name = "id")
    private Long id;

    @OneToOne (cascade = CascadeType.ALL)
    @MapsId
    @JoinColumn(name ="id")
    private AuthenticationUser user;


    private UUID in_match;
    private int in_match_team;

}


