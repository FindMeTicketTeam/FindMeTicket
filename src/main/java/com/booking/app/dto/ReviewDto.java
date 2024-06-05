package com.booking.app.dto;

import com.booking.app.entity.Review;
import com.booking.app.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.authentication.ProviderNotFoundException;

import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "Data Transfer Object for representing a review.")
public class ReviewDto {

    @Schema(description = "The unique identifier of the review.", example = "123e4567-e89b-12d3-a456-556642440000")
    private UUID id;

    @Schema(example = "The best service.", description = "The text of the review.")
    private String reviewText;

    @Schema(description = "The date when the review was written.", example = "2024-05-30")
    private String writingDate;

    @Schema(description = "The grade given for the review.", example = "4")
    private int grade;

    @Schema(description = "The username of the reviewer.", example = "john_doe")
    private String username;

    @Schema(description = "The default avatar of the reviewer.")
    private byte[] defaultAvatar;

    @Schema(description = "The URL of the social media avatar of the reviewer.", example = "https://example.com/avatar.png")
    private String socialMediaAvatar;

    public static ReviewDto createInstance(Review review) {
        User user = review.getUser();
        ReviewDto reviewDTO = ReviewDto.builder()
                .id(user.getReview().getId())
                .writingDate(review.getAddingDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")))
                .grade(review.getGrade())
                .reviewText(review.getReviewText())
                .username(user.getUsername())
                .build();

        switch (user.getProvider()) {
            case GOOGLE -> reviewDTO.setSocialMediaAvatar(user.getSocialMediaAvatar());
            case LOCAL -> reviewDTO.setDefaultAvatar(user.getDefaultAvatar());
            default -> throw new ProviderNotFoundException("Third party service provider is not provided");
        }
        return reviewDTO;
    }
}
