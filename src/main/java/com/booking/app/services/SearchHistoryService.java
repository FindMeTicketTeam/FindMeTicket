package com.booking.app.services;

import com.booking.app.dto.RequestTicketsDTO;
import com.booking.app.dto.SearchHistoryDto;
import com.booking.app.entity.UserSearchHistory;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.UUID;

public interface SearchHistoryService {

    void addToHistory(RequestTicketsDTO requestTicketsDTO, String language, HttpServletRequest request);

    List<SearchHistoryDto> getUserHistory(HttpServletRequest request);
}
