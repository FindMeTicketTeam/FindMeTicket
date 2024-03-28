package com.booking.app.repositories;

import com.booking.app.entity.UserSearchHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SearchHistoryRepository extends JpaRepository<UserSearchHistory, UUID> {

}
