package com.booking.app.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "roles")
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Builder
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Short id;

    @Enumerated(EnumType.STRING)
    private RoleType type;

    @ManyToMany(mappedBy = "roles")
    private Set<User> users;

    public static Set<Role> createRoles(Role... roles) {
        return new HashSet<>(Arrays.asList(roles));
    }

    public Set<SimpleGrantedAuthority> getGrantedAuthorities() {
        return Collections.singleton(new SimpleGrantedAuthority("ROLE_" + type.getType()));
    }

    @Getter
    public enum RoleType {
        ADMIN("ADMIN"),
        USER("USER");

        private final String type;

        RoleType(String type) {
            this.type = type;
        }

    }

}
