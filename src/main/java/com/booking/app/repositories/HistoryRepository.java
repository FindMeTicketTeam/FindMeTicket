package com.booking.app.repositories;

import com.booking.app.entity.SearchHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface HistoryRepository extends JpaRepository<SearchHistory, UUID> {

}
