package com.booking.app.controller;

import com.booking.app.constant.CustomHttpHeaders;
import com.booking.app.constant.JwtTokenConstants;
import com.booking.app.dto.LoginDTO;
import com.booking.app.dto.OAuth2IdTokenDTO;
import com.booking.app.entity.UserCredentials;
import com.booking.app.security.jwt.JwtProvider;
import com.booking.app.services.GoogleAccountService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@ExtendWith(MockitoExtension.class)
class LoginControllerTest {

    @InjectMocks
    private LoginController loginController;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtProvider jwtProvider;
    @Mock
    private GoogleAccountService googleAccountService;
    @Mock
    private Authentication authentication;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = standaloneSetup(loginController).build();
    }

    @Test
    void login_rightCredentials_ok() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();

        LoginDTO loginDTO = LoginDTO.builder().email("mishaakamichael999@gmail.com")
                .password("michael999")
                .rememberMe(true)
                .build();
        UserCredentials userCredentials = UserCredentials.builder().id(UUID.randomUUID()).build();

        when(authenticationManager.authenticate(any())).thenReturn(authentication);

        doReturn(true).when(authentication).isAuthenticated();

        when(authentication.getPrincipal()).thenReturn(userCredentials);
        when(jwtProvider.generateRefreshToken(Mockito.anyString())).thenReturn("mockedRefreshToken");
        when(jwtProvider.generateAccessToken(Mockito.anyString())).thenReturn("mockedAccessToken");

        mockMvc.perform(post("/login")
                        .content(objectMapper.writeValueAsString(loginDTO))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.header().exists(CustomHttpHeaders.USER_ID))
                .andExpect(MockMvcResultMatchers.header().exists(CustomHttpHeaders.REMEMBER_ME))
                .andExpect(MockMvcResultMatchers.header().exists(HttpHeaders.AUTHORIZATION))
                .andExpect(MockMvcResultMatchers.cookie().exists(JwtTokenConstants.REFRESH_TOKEN));
    }

    @Test
    void login_invalidCredentials_unauthorized() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();

        LoginDTO loginDTO = LoginDTO.builder().email("mishaakamichael999@gmail.com")
                .password("michael999")
                .rememberMe(true)
                .build();

        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(false);

        mockMvc.perform(post("/login")
                        .content(objectMapper.writeValueAsString(loginDTO))
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(MockMvcResultMatchers.status().isUnauthorized())
                .andExpect(MockMvcResultMatchers.header().doesNotExist(CustomHttpHeaders.USER_ID))
                .andExpect(MockMvcResultMatchers.header().doesNotExist(CustomHttpHeaders.REMEMBER_ME))
                .andExpect(MockMvcResultMatchers.header().doesNotExist(HttpHeaders.AUTHORIZATION))
                .andExpect(MockMvcResultMatchers.cookie().doesNotExist(JwtTokenConstants.REFRESH_TOKEN));
    }

    @Test
    void loginOAuth2_validAccess_ok() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();

        OAuth2IdTokenDTO tokenDTO = OAuth2IdTokenDTO.builder().idToken("token777").build();
        UserCredentials userCredentials = UserCredentials.builder()
                .id(UUID.randomUUID())

                .email("mishaakamichael999@gmail.com").build();
        when(googleAccountService.loginOAuthGoogle(any())).thenReturn(userCredentials);
        when(jwtProvider.generateRefreshToken(Mockito.anyString())).thenReturn("mockedRefreshToken");
        when(jwtProvider.generateAccessToken(Mockito.anyString())).thenReturn("mockedAccessToken");

        mockMvc.perform(post("/oauth2/authorize/*")
                        .content(objectMapper.writeValueAsString(tokenDTO))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.header().exists(CustomHttpHeaders.USER_ID))
                .andExpect(MockMvcResultMatchers.header().exists(HttpHeaders.AUTHORIZATION))
                .andExpect(MockMvcResultMatchers.cookie().exists(JwtTokenConstants.REFRESH_TOKEN));
    }

}
