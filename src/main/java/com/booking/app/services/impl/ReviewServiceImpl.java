package com.booking.app.services.impl;

import com.booking.app.dto.ReviewDto;
import com.booking.app.dto.SaveReviewDto;
import com.booking.app.entity.Review;
import com.booking.app.entity.User;
import com.booking.app.repositories.ReviewRepository;
import com.booking.app.repositories.UserRepository;
import com.booking.app.services.ReviewService;
import com.booking.app.util.CookieUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static com.booking.app.constant.CustomHttpHeaders.USER_ID;

@Service
@Log4j2
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {
    // todo refactor methods
    private final ReviewRepository reviewRepository;

    private final UserRepository userRepository;

    @Override
    public ReviewDto saveReview(SaveReviewDto saveReviewDto, HttpServletRequest request) {
        return findUser(request)
                .map(user -> {
                    Review existingReview = user.getReview();
                    if (Objects.nonNull(existingReview)) {
                        reviewRepository.delete(existingReview);
                    }

                    Review newReview = Review.builder()
                            .reviewText(saveReviewDto.getReviewText())
                            .grade(saveReviewDto.getGrade())
                            .user(user)
                            .build();

                    user.setReview(newReview);
                    reviewRepository.save(newReview);

                    return ReviewDto.createInstance(newReview);
                })
                .orElse(null);
    }

    @Override
    public List<ReviewDto> getReviewList() {
        List<Review> reviewList = reviewRepository.findAll();
        return reviewList.stream().map(ReviewDto::createInstance).toList();
    }

    @Override
    public boolean deleteReview(HttpServletRequest request) {
        return findUser(request).map(user -> {
            Review review = user.getReview();
            if (Objects.nonNull(review)) {
                reviewRepository.delete(review);
                return true;
            }
            return false;
        }).orElse(false);
    }

    @Override
    public ReviewDto getUserReview(HttpServletRequest request) {
        return findUser(request)
                .map(User::getReview)
                .map(ReviewDto::createInstance)
                .orElse(null);
    }
    // todo thrown exception

    /**
     * Finds the user based on the HTTP request.
     *
     * @param request The HTTP request.
     * @return Optional of User.
     */
    @NotNull
    private Optional<User> findUser(HttpServletRequest request) {
        Optional<UUID> uuid = CookieUtils.getCookie(request, USER_ID)
                .map(cookie -> UUID.fromString(cookie.getValue()));
        return uuid.flatMap(userRepository::findById);
    }

}
