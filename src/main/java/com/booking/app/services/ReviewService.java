package com.booking.app.services;

import com.booking.app.dto.ReviewDto;
import com.booking.app.dto.SaveReviewDto;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

public interface ReviewService {

    /**
     * Saves a review provided by the user.
     *
     * @param saveReviewDto The DTO containing review information.
     * @param request       The HTTP request.
     * @return The saved review DTO if successful, or null otherwise.
     */
    ReviewDto saveReview(SaveReviewDto saveReviewDto, HttpServletRequest request);

    /**
     * Retrieves a list of all reviews.
     *
     * @return List of review DTOs.
     */
    List<ReviewDto> getReviewList();

    /**
     * Deletes a review provided by the user.
     *
     * @param request The HTTP request.
     * @return True if the review is deleted successfully, false otherwise.
     */
    boolean deleteReview(HttpServletRequest request);

    /**
     * Retrieves the review of the current user.
     *
     * @param request The HTTP request.
     * @return The review DTO of the current user, or null if not found.
     */
    ReviewDto getUserReview(HttpServletRequest request);
}
