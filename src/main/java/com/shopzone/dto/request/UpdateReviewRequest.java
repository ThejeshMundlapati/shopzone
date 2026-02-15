package com.shopzone.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateReviewRequest {

  @Min(value = 1, message = "Rating must be at least 1")
  @Max(value = 5, message = "Rating must be at most 5")
  private Integer rating;

  @Size(max = 100, message = "Title must be at most 100 characters")
  private String title;

  @Size(max = 2000, message = "Comment must be at most 2000 characters")
  private String comment;
}