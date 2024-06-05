package com.booking.app.services;

import com.booking.app.entity.User;

public interface NotificationService {

    /**
     * Disables notifications for a given user.
     *
     * @param user the user for whom notifications are to be disabled
     */
    void disable(User user);

    /**
     * Enables notifications for a given user.
     *
     * @param user the user for whom notifications are to be enabled
     */
    void enable(User user);
}
