package com.booking.app.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "users")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@EntityListeners(AuditingEntityListener.class)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "registration_date")
    @CreatedDate
    private LocalDate registrationDate;

    @Column(name = "phone_number", unique = true)
    private String phoneNumber;

    @Column(name = "profile_picture", columnDefinition = "BYTEA")
    private byte[] profilePicture;

    private String urlPicture;

    private Boolean notification;

    @ManyToOne
    @JoinColumn(referencedColumnName = "id", name = "role_id")
    private Role role;

    @JoinColumn(referencedColumnName = "id", name = "token_id")
    @OneToOne(cascade = CascadeType.ALL)
    private ConfirmToken confirmToken;

    @OneToOne(mappedBy = "user")
    private UserCredentials security;

    @OneToMany(mappedBy = "user" ,fetch = FetchType.EAGER,cascade = CascadeType.ALL)
    private List<UserSearchHistory> history;
}
