package com.booking.app.controller;

import com.booking.app.dto.CitiesDTO;
import com.booking.app.dto.StartLettersDTO;
import com.booking.app.services.TypeAheadService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@ExtendWith(MockitoExtension.class)
class TypeAheadControllerTest {
    @InjectMocks
    private TypeAheadController typeAheadController;

    @Mock
    private TypeAheadService typeAheadService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = standaloneSetup(typeAheadController).build();
    }

    @Test
    void getCities_success() throws IOException {

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(HttpHeaders.CONTENT_LANGUAGE, "en");

        StartLettersDTO startLettersDTO = new StartLettersDTO("Dn");

        List<CitiesDTO> expectedCities = List.of(
                CitiesDTO.builder().country("UA").cityUa("Дніпро").cityEng("Dnipro").build(),
                CitiesDTO.builder().country("UA").cityUa("Дніпровка").cityEng("Dniprovka").build(),
                CitiesDTO.builder().country("UA").cityUa("Дніпрорудне").cityEng("Dniprorudne").build()
        );

        when(typeAheadService.findMatchesUA(any(StartLettersDTO.class), any(String.class)))
                .thenReturn(expectedCities);

        ResponseEntity<List<CitiesDTO>> responseEntity = typeAheadController.getCities(startLettersDTO, request);

        assertEquals(200, responseEntity.getStatusCodeValue());
        assertEquals(expectedCities, responseEntity.getBody());
    }

}
