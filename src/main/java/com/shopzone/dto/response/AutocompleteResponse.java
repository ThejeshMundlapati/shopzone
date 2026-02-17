package com.shopzone.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AutocompleteResponse {

  private String query;
  private List<Suggestion> suggestions;
  private long searchTimeMs;

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class Suggestion {
    private String text;
    private String type;
    private String id;
    private String slug;
    private Double price;
    private String imageUrl;
    private Double score;
  }
}