package com.booking.app.services.impl;

import com.booking.app.dto.EmailDTO;
import com.booking.app.dto.RegistrationDTO;
import com.booking.app.dto.TokenConfirmationDTO;
import com.booking.app.entity.ConfirmToken;
import com.booking.app.entity.Role;
import com.booking.app.entity.User;
import com.booking.app.entity.UserCredentials;
import com.booking.app.enums.EnumProvider;
import com.booking.app.enums.EnumRole;
import com.booking.app.exception.exception.EmailAlreadyExistsException;
import com.booking.app.exception.exception.UsernameAlreadyExistsException;
import com.booking.app.mapper.UserMapper;
import com.booking.app.repositories.RoleRepository;
import com.booking.app.repositories.UserCredentialsRepository;
import com.booking.app.repositories.VerifyEmailRepository;
import com.booking.app.services.MailSenderService;
import com.booking.app.services.RegistrationService;
import com.booking.app.services.TokenService;
import com.talanlabs.avatargenerator.Avatar;
import com.talanlabs.avatargenerator.GitHubAvatar;
import com.talanlabs.avatargenerator.layers.backgrounds.ColorPaintBackgroundLayer;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.*;
import java.util.Optional;
import java.util.Random;

/**
 * Service class for user registration operations.
 */
@Service
@RequiredArgsConstructor
public class RegistrationServiceImpl implements RegistrationService {

    private final UserCredentialsRepository userCredentialsRepository;
    private final RoleRepository roleRepository;
    private final VerifyEmailRepository verifyEmailRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper mapper;
    private final MailSenderService mailService;
    private final TokenService tokenService;

    /**
     * Registers a new user based on the provided registration information.
     *
     * @param registrationDTO The RegistrationDTO containing user registration details.
     * @return EmailDTO Returns an EmailDTO containing information about the registration confirmation email.
     * @throws EmailAlreadyExistsException    If a user with the provided email already exists.
     * @throws UsernameAlreadyExistsException If a user with the provided username already exists.
     * @throws MessagingException             If there is an issue with sending the confirmation email.
     */
    @Override
    public EmailDTO register(RegistrationDTO registrationDTO) throws MessagingException {
        Optional<UserCredentials> userCredentials = userCredentialsRepository.findByEmailOrUsername(registrationDTO.getEmail(), registrationDTO.getUsername());
        if (userCredentials.isPresent()
                && userCredentials.get().getEmail().equals(registrationDTO.getEmail())
                && userCredentials.get().isEnabled()) {

            throw new EmailAlreadyExistsException("We’re sorry. This email already exists");
        }

        if (userCredentials.isPresent()
                && userCredentials.get().getUsername().equals(registrationDTO.getUsername())
                && userCredentials.get().isEnabled()) {
            throw new UsernameAlreadyExistsException("We’re sorry. This username already exists");
        }
        if (userCredentials.isPresent()) {
            deleteUserIfNotConfirmed(userCredentials.get());
        }

        return performRegistration(registrationDTO);
    }

    /**
     * Deletes user
     *
     * @param byEmail UserCredentials that must be deleted
     */
    @Transactional
    public void deleteUserIfNotConfirmed(UserCredentials byEmail) {
        userCredentialsRepository.deleteById(byEmail.getId());
    }

    /**
     * Performs registration:
     * Creates user, token
     * Sends email
     *
     * @param registrationDTO The RegistrationDTO containing user registration details.
     * @return EmailDTO containing email
     * @throws MessagingException If there is an issue with sending the confirmation email.
     */
    //    @Transactional
    public EmailDTO performRegistration(RegistrationDTO registrationDTO) throws MessagingException {
        UserCredentials securityEntity = mapper.toUserSecurity(registrationDTO);
        securityEntity.setProvider(EnumProvider.LOCAL);
        securityEntity.setPassword(passwordEncoder.encode(registrationDTO.getPassword()));

        User user = createNewRegisteredUser(securityEntity, registrationDTO.getNotification());

        mailService.sendEmail("confirmMailUa", "Email confirmation", user.getConfirmToken().getToken(), securityEntity);

        return mapper.toEmailDTO(securityEntity);
    }

    /**
     * Generates a token, saves user to the Database
     *
     * @param userCredentials UserSecurity that must be saved
     * @return User that was saved
     */
    @Transactional
    public User createNewRegisteredUser(UserCredentials userCredentials, Boolean notification) {
        Role role = roleRepository.findRoleByEnumRole(EnumRole.USER);

        Avatar avatar = GitHubAvatar.newAvatarBuilder().layers(new ColorPaintBackgroundLayer(Color.WHITE)).build();
        byte[] asPngBytes = avatar.createAsPngBytes(new Random().nextLong());

        User user = User.builder()
                .notification(notification)
                .security(userCredentials)
                .profilePicture(asPngBytes)
                .role(role)
                .build();

        ConfirmToken confirmToken = tokenService.createConfirmToken(user);

        userCredentials.setUser(user);
        user.setConfirmToken(confirmToken);

        userCredentialsRepository.save(userCredentials);

        return user;
    }

    /**
     * Enables a user if the provided token confirmation details are valid.
     *
     * @param dto The TokenConfirmationDTO containing token confirmation details.
     * @return boolean Returns true if the user is successfully enabled; otherwise, returns false.
     */
    @Transactional
    @Override
    public boolean enableIfValid(TokenConfirmationDTO dto) {
        Optional<UserCredentials> userByEmail = userCredentialsRepository.findByEmail(dto.getEmail());

        if (userByEmail.isPresent() && !userByEmail.get().isEnabled() && tokenService.verifyToken(dto.getEmail(), dto.getToken())) {
            userCredentialsRepository.enableUser(userByEmail.get().getId());
            return true;

        }
        return false;
    }

}
