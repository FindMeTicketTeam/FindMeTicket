package com.booking.app.services.impl;

import com.booking.app.dto.EmailDTO;
import com.booking.app.dto.RegistrationDTO;
import com.booking.app.dto.TokenConfirmationDTO;
import com.booking.app.entity.ConfirmToken;
import com.booking.app.entity.Role;
import com.booking.app.entity.User;
import com.booking.app.entity.UserCredentials;
import com.booking.app.exception.exception.EmailAlreadyExistsException;
import com.booking.app.exception.exception.UsernameAlreadyExistsException;
import com.booking.app.mapper.UserMapper;
import com.booking.app.repositories.RoleRepository;
import com.booking.app.repositories.UserCredentialsRepository;
import com.booking.app.repositories.VerifyEmailRepository;
import com.booking.app.services.MailSenderService;
import com.booking.app.services.TokenService;
import jakarta.mail.MessagingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegistrationServiceImplTest {
    @InjectMocks
    private RegistrationServiceImpl registrationService;

    @Mock
    private UserCredentialsRepository userCredentialsRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private VerifyEmailRepository verifyEmailRepository;

    @Mock
    private UserMapper mapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private MailSenderService mailService;

    @Mock
    private TokenService tokenService;


    private UserCredentials userCredentials;
    private ConfirmToken confirmToken;
    private User user;
    private RegistrationDTO registrationDTO;
    private TokenConfirmationDTO tokenConfirmationDTO;
    private Role role;

    @BeforeEach
    void setUp() {
        registrationDTO = RegistrationDTO.builder().email("mishaakamichael999@gmail.com")
                .username("Michael999")
                .password("GloryToUkraine5")
                .confirmPassword("GloryToUkraine5").build();
    }

    @Test
    void testSuccessfullyRegistration() throws MessagingException {
        RegistrationServiceImpl temp = Mockito.spy(registrationService);
        when(userCredentialsRepository.findByEmailOrUsername(registrationDTO.getEmail(), registrationDTO.getUsername())).thenReturn(Optional.empty());
        doReturn(new EmailDTO(registrationDTO.getEmail())).when(temp).performRegistration(registrationDTO);
        EmailDTO newUser = temp.register(registrationDTO);
        assertEquals(registrationDTO.getEmail(), newUser.getEmail());
    }

    @Test
    void testEmailExistsExceptionRegistration() throws MessagingException {
        when(userCredentialsRepository.findByEmailOrUsername(registrationDTO.getEmail(), registrationDTO.getUsername()))
                .thenReturn(Optional.of(UserCredentials.builder().email(registrationDTO.getEmail()).username("Lalka228").enabled(true).build()));

        assertThrows(EmailAlreadyExistsException.class, () -> registrationService.register(registrationDTO));
    }

    @Test
    void testUsernameExistsExceptionRegistration() throws MessagingException {
        when(userCredentialsRepository.findByEmailOrUsername(registrationDTO.getEmail(), registrationDTO.getUsername()))
                .thenReturn(Optional.of(UserCredentials.builder().email("rafaello2@gmail.com").username(registrationDTO.getUsername()).enabled(true).build()));

        assertThrows(UsernameAlreadyExistsException.class, () -> registrationService.register(registrationDTO));
    }

    @Test
    void testDeletesUnconfirmedUserRegistration() throws MessagingException {
        RegistrationServiceImpl temp = Mockito.spy(registrationService);
        UserCredentials userCredentials = UserCredentials.builder().email("hasbulla@gmail.com").username("hasbulla23").build();
        when(userCredentialsRepository.findByEmailOrUsername(registrationDTO.getEmail(), registrationDTO.getUsername())).thenReturn(Optional.of(userCredentials));
        doNothing().when(temp).deleteUserIfNotConfirmed(userCredentials);
        doReturn(new EmailDTO(registrationDTO.getEmail())).when(temp).performRegistration(registrationDTO);
        EmailDTO newUser = temp.register(registrationDTO);
        assertEquals(registrationDTO.getEmail(), newUser.getEmail());
    }

    @Test
    void testDeleteUserIfNotConfirmed() {

        UUID id = new UUID(9583894, 34757);
        ConfirmToken token = ConfirmToken.builder().id(id).build();
        User user = User.builder().confirmToken(token).build();
        UserCredentials userCredentials = UserCredentials.builder().id(id).email("javier_milei@gmail.com").username("Javier Milei").user(user).build();

        doNothing().when(userCredentialsRepository).deleteById(id);
        assertDoesNotThrow(() -> registrationService.deleteUserIfNotConfirmed(userCredentials));
    }

//    @Test
//    void testPerformRegistration() throws MessagingException {
//        RegistrationServiceImpl temp = Mockito.spy(registrationService);
//
//        UserCredentials userCredentials = UserCredentials.builder().username(registrationDTO.getUsername()).email(registrationDTO.getEmail())
//                .password(registrationDTO.getPassword()).build();
//
//        when(mapper.toUserSecurity(registrationDTO)).thenReturn(userCredentials);
//
//        Instant now = Instant.now();
//        Instant later = now.minusSeconds(600);
//        Date dateAfter10Minutes = Date.from(later);
//
//        ConfirmToken token = ConfirmToken.builder().token("SAD88").expiryTime(dateAfter10Minutes).build();
//
//        User user = User.builder().confirmToken(token).build();
//
//        doReturn(user).when(temp).createNewRegisteredUser(userCredentials);
//        when(mapper.toEmailDTO(userCredentials)).thenReturn(new EmailDTO(userCredentials.getEmail()));
//
//        EmailDTO emailDTO = temp.performRegistration(registrationDTO);
//
//        assertEquals(userCredentials.getEmail(), emailDTO.getEmail());
//    }

    @Test
    void testSuccessEnableUserIfValid() {

        TokenConfirmationDTO dto = TokenConfirmationDTO.builder().token("SAD88").email("javier_milei@gmail.com").build();

        UserCredentials userCredentials = UserCredentials.builder().enabled(false).build();

        when(tokenService.verifyToken(dto.getEmail(), dto.getToken())).thenReturn(true);

        when(userCredentialsRepository.findByEmail(dto.getEmail())).thenReturn(Optional.of(userCredentials));

        assertTrue(registrationService.enableIfValid(dto));
    }

    @Test
    void testEnableUserIfValidWrongToken() {

        TokenConfirmationDTO dto = TokenConfirmationDTO.builder().token("SAD88").email("javier_milei@gmail.com").build();

        UserCredentials userCredentials = UserCredentials.builder().enabled(false).build();

        when(tokenService.verifyToken(dto.getEmail(), dto.getToken())).thenReturn(false);

        when(userCredentialsRepository.findByEmail(dto.getEmail())).thenReturn(Optional.of(userCredentials));

        assertFalse(registrationService.enableIfValid(dto));
    }

    @Test
    void testEnableUserIfValidNoUserFound() {

        TokenConfirmationDTO dto = TokenConfirmationDTO.builder().token("SAD88").email("javier_milei@gmail.com").build();

        when(userCredentialsRepository.findByEmail(dto.getEmail())).thenReturn(Optional.empty());

        assertFalse(registrationService.enableIfValid(dto));
    }

    @Test
    void testEnableUserIfValidUserAlreadyEnabled() {

        TokenConfirmationDTO dto = TokenConfirmationDTO.builder().token("SAD88").email("javier_milei@gmail.com").build();

        UserCredentials userCredentials = UserCredentials.builder().enabled(true).build();

        when(userCredentialsRepository.findByEmail(dto.getEmail())).thenReturn(Optional.of(userCredentials));

        assertFalse(registrationService.enableIfValid(dto));
    }

}