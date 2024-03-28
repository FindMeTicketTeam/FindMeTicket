package com.booking.app.controller;

import com.booking.app.controller.api.SearchHistoryAPI;
import com.booking.app.services.SearchHistoryService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class SearchHistoryController implements SearchHistoryAPI {

    private final SearchHistoryService searchHistoryService;

    @GetMapping("/getHistory")
    @PreAuthorize("#{hasAnyRole('USER', 'ADMIN')}")
    @Override
    public ResponseEntity<?> getHistory(HttpServletRequest request) {

        try {
            return ResponseEntity.ok().body(searchHistoryService.getUserHistory(request));
        } catch (UsernameNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
