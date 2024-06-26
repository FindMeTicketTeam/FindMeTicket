package com.booking.app.services.impl;

import com.booking.app.dto.OAuth2IdTokenDTO;
import com.booking.app.entity.Role;
import com.booking.app.entity.User;
import com.booking.app.entity.UserCredentials;
import com.booking.app.enums.EnumProvider;
import com.booking.app.enums.EnumRole;
import com.booking.app.repositories.RoleRepository;
import com.booking.app.repositories.UserCredentialsRepository;
import com.booking.app.services.GoogleAccountService;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
public class GoogleAccountServiceImpl implements GoogleAccountService {

    private final UserCredentialsRepository userCredentialsRepository;
    private final RoleRepository roleRepository;
    private final GoogleIdTokenVerifier verifier;


    @Autowired
    public GoogleAccountServiceImpl(@Value("${app.googleClientId}") String clientId, UserCredentialsRepository userCredentialsRepository, RoleRepository roleRepository) {
        this.userCredentialsRepository = userCredentialsRepository;
        this.roleRepository = roleRepository;
        NetHttpTransport transport = new NetHttpTransport();
        JsonFactory jsonFactory = new GsonFactory();
        verifier = new GoogleIdTokenVerifier.Builder(transport, jsonFactory)
                .setAudience(Collections.singletonList(clientId))
                .build();
    }

    /**
     * Login a user using Google OAuth and create or update the user's credentials.
     *
     * @param requestBody DTO containing the Google OAuth2 ID Token.
     * @return UserCredentials for the logged-in user.
     */
    public Optional<UserCredentials> loginOAuthGoogle(OAuth2IdTokenDTO requestBody) {
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
     * @return UserCredentials for the created or updated user.
     */
    private UserCredentials createOrUpdateUser(GoogleIdToken googleIdToken) {
        GoogleIdToken.Payload payload = googleIdToken.getPayload();
        UserCredentials existingAccount = userCredentialsRepository.findByEmail(payload.getEmail()).orElse(null);
        Role role = roleRepository.findRoleByEnumRole(EnumRole.USER);
        if (existingAccount == null) {
            User user = User.builder()
                    .security(
                            UserCredentials.builder()
                                    .provider(EnumProvider.GOOGLE)
                                    .username(payload.get("given_name") + " " + payload.get("family_name"))
                                    .email(payload.getEmail())
                                    .enabled(true)
                                    .accountNonExpired(true)
                                    .credentialsNonExpired(true)
                                    .accountNonLocked(true)
                                    .build()
                    )
                    .urlPicture((String) payload.get("picture"))
                    .role(role)
                    .build();
            user.getSecurity().setUser(user);
            return userCredentialsRepository.save(user.getSecurity());
        }
        existingAccount.setUsername(payload.get("given_name") + " " + payload.get("family_name"));
        existingAccount.getUser().setUrlPicture((String) payload.get("picture"));
        userCredentialsRepository.save(existingAccount);
        return existingAccount;
    }

}
