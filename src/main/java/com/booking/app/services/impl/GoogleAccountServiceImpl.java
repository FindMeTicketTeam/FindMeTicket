package com.booking.app.services.impl;

import com.booking.app.dto.SocialLoginDto;
import com.booking.app.entity.Role;
import com.booking.app.entity.User;
import com.booking.app.enums.EnumRole;
import com.booking.app.repositories.RoleRepository;
import com.booking.app.repositories.UserRepository;
import com.booking.app.services.GoogleAccountService;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Optional;

/**
 * Service implementation for Google Account-related operations.
 * <p>
 * This service provides functionality for handling Google OAuth login and user creation or update.
 */
@Service
@RequiredArgsConstructor
public class GoogleAccountServiceImpl implements GoogleAccountService {

    private final UserRepository userRepository;

    private final RoleRepository roleRepository;
    @Value("${app.googleClientId}")
    String clientId;
    private GoogleIdTokenVerifier verifier;

    @PostConstruct
    void setGoogleIdTokenVerifier() {
        NetHttpTransport transport = new NetHttpTransport();
        JsonFactory jsonFactory = new GsonFactory();
        verifier = new GoogleIdTokenVerifier.Builder(transport, jsonFactory)
                .setAudience(Collections.singletonList(clientId))
                .build();
    }

    /**
     * Login a user using Google OAuth and create or update the user
     *
     * @param requestBody DTO containing the Google OAuth2 ID Token.
     * @return User for the logged-in user.
     */
    public Optional<User> loginOAuthGoogle(SocialLoginDto requestBody) {
        try {
            GoogleIdToken account = verifier.verify(requestBody.getIdToken());
            return Optional.of(createOrUpdateUser(account));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     * Create or update a user based on the information in the provided Google ID Token.
     *
     * @param googleIdToken Google ID Token containing user information.
     * @return User for the created or updated user.
     */
    private User createOrUpdateUser(GoogleIdToken googleIdToken) {
        GoogleIdToken.Payload payload = googleIdToken.getPayload();
        User existingAccount = userRepository.findByEmail(payload.getEmail()).orElse(null);
        Role role = roleRepository.findRoleByEnumRole(EnumRole.USER);
        if (existingAccount == null) {
            User user = User.createGoogleUser(
                    role,
                    payload.get("given_name") + " " + payload.get("family_name"),
                    payload.getEmail(),
                    (String) payload.get("picture")
            );
            return userRepository.save(user);
        }
        existingAccount.setUsername(payload.get("given_name") + " " + payload.get("family_name"));
        existingAccount.setSocialMediaAvatar((String) payload.get("picture"));
        userRepository.save(existingAccount);
        return existingAccount;
    }

}
