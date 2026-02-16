package com.shopzone.service;

import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import com.shopzone.dto.request.ProductSearchRequest;
import com.shopzone.dto.response.AutocompleteResponse;
import com.shopzone.dto.response.SearchResultResponse;
import com.shopzone.model.elasticsearch.ProductDocument;
import com.shopzone.repository.elasticsearch.ProductSearchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductSearchService {

  private final ProductSearchRepository productSearchRepository;
  private final ElasticsearchOperations elasticsearchOperations;

  public SearchResultResponse search(ProductSearchRequest request) {
    long startTime = System.currentTimeMillis();

    NativeQuery query = buildSearchQuery(request);

    SearchHits<ProductDocument> searchHits = elasticsearchOperations.search(
        query,
        ProductDocument.class
    );

    List<SearchResultResponse.ProductHit> products = searchHits.getSearchHits().stream()
        .map(hit -> SearchResultResponse.ProductHit.from(hit.getContent(), hit.getScore()))
        .collect(Collectors.toList());

    long totalHits = searchHits.getTotalHits();
    int totalPages = (int) Math.ceil((double) totalHits / request.getSize());

    long searchTime = System.currentTimeMillis() - startTime;

    return SearchResultResponse.builder()
        .products(products)
        .totalHits(totalHits)
        .totalPages(totalPages)
        .currentPage(request.getPage())
        .pageSize(request.getSize())
        .query(request.getQuery())
        .searchTimeMs(searchTime)
        .build();
  }

  public AutocompleteResponse autocomplete(String prefix, int limit) {
    long startTime = System.currentTimeMillis();

    if (prefix == null || prefix.trim().length() < 2) {
      return AutocompleteResponse.builder()
          .query(prefix)
          .suggestions(Collections.emptyList())
          .searchTimeMs(0)
          .build();
    }

    String cleanPrefix = prefix.trim().toLowerCase();

    NativeQuery query = NativeQuery.builder()
        .withQuery(q -> q
            .bool(b -> b
                .should(s -> s
                    .prefix(p -> p
                        .field("name")
                        .value(cleanPrefix)
                        .boost(3.0f)
                    )
                )
                .should(s -> s
                    .prefix(p -> p
                        .field("brand")
                        .value(cleanPrefix)
                        .boost(2.0f)
                    )
                )
                .should(s -> s
                    .match(m -> m
                        .field("name")
                        .query(cleanPrefix)
                        .fuzziness("AUTO")
                    )
                )
                .minimumShouldMatch("1")
            )
        )
        .withPageable(PageRequest.of(0, limit))
        .build();

    SearchHits<ProductDocument> hits = elasticsearchOperations.search(
        query,
        ProductDocument.class
    );

    List<AutocompleteResponse.Suggestion> suggestions = hits.getSearchHits().stream()
        .map(hit -> {
          ProductDocument doc = hit.getContent();
          String imageUrl = doc.getImages() != null && !doc.getImages().isEmpty()
              ? doc.getImages().get(0) : null;

          return AutocompleteResponse.Suggestion.builder()
              .text(doc.getName())
              .type("product")
              .id(doc.getId())
              .slug(doc.getSlug())
              .price(doc.getPrice())
              .imageUrl(imageUrl)
              .score((double) hit.getScore())
              .build();
        })
        .collect(Collectors.toList());

    long searchTime = System.currentTimeMillis() - startTime;

    return AutocompleteResponse.builder()
        .query(prefix)
        .suggestions(suggestions)
        .searchTimeMs(searchTime)
        .build();
  }

  public List<ProductDocument> findSimilarProducts(String productId, int limit) {
    Optional<ProductDocument> productOpt = productSearchRepository.findById(productId);
    if (productOpt.isEmpty()) {
      return Collections.emptyList();
    }

    ProductDocument product = productOpt.get();

    NativeQuery query = NativeQuery.builder()
        .withQuery(q -> q
            .bool(b -> {
              BoolQuery.Builder builder = b
                  .mustNot(mn -> mn
                      .term(t -> t.field("_id").value(productId))
                  )
                  .must(m -> m
                      .term(t -> t.field("active").value(true))
                  );

              if (product.getCategoryId() != null) {
                builder.should(s -> s
                    .term(t -> t
                        .field("categoryId")
                        .value(product.getCategoryId())
                        .boost(2.0f)
                    )
                );
              }

              if (product.getBrand() != null) {
                builder.should(s -> s
                    .term(t -> t
                        .field("brand")
                        .value(product.getBrand())
                        .boost(1.5f)
                    )
                );
              }

              if (product.getPrice() != null) {
                double minPrice = product.getPrice() * 0.7;
                double maxPrice = product.getPrice() * 1.3;
                builder.should(s -> s
                    .range(r -> r
                        .field("price")
                        .gte(co.elastic.clients.json.JsonData.of(minPrice))
                        .lte(co.elastic.clients.json.JsonData.of(maxPrice))
                    )
                );
              }

              return builder.minimumShouldMatch("1");
            })
        )
        .withPageable(PageRequest.of(0, limit))
        .build();

    SearchHits<ProductDocument> hits = elasticsearchOperations.search(
        query,
        ProductDocument.class
    );

    return hits.getSearchHits().stream()
        .map(SearchHit::getContent)
        .collect(Collectors.toList());
  }

  private NativeQuery buildSearchQuery(ProductSearchRequest request) {
    return NativeQuery.builder()
        .withQuery(q -> q
            .bool(b -> {
              BoolQuery.Builder builder = b;

              if (request.getQuery() != null && !request.getQuery().isBlank()) {
                builder.must(m -> m
                    .multiMatch(mm -> mm
                        .query(request.getQuery())
                        .fields("name^3", "description^2", "brand^2", "tags")
                        .fuzziness("AUTO")
                        .prefixLength(2)
                    )
                );
              }

              builder.filter(f -> f
                  .term(t -> t.field("active").value(true))
              );

              if (request.getMinPrice() != null || request.getMaxPrice() != null) {
                builder.filter(f -> f
                    .range(r -> {
                      var range = r.field("price");
                      if (request.getMinPrice() != null) {
                        range.gte(co.elastic.clients.json.JsonData.of(request.getMinPrice()));
                      }
                      if (request.getMaxPrice() != null) {
                        range.lte(co.elastic.clients.json.JsonData.of(request.getMaxPrice()));
                      }
                      return range;
                    })
                );
              }

              if (request.getCategoryId() != null && !request.getCategoryId().isBlank()) {
                builder.filter(f -> f
                    .term(t -> t.field("categoryId").value(request.getCategoryId()))
                );
              }

              if (request.getBrand() != null && !request.getBrand().isBlank()) {
                builder.filter(f -> f
                    .term(t -> t.field("brand").value(request.getBrand()))
                );
              }

              if (request.getMinRating() != null) {
                builder.filter(f -> f
                    .range(r -> r
                        .field("averageRating")
                        .gte(co.elastic.clients.json.JsonData.of(request.getMinRating()))
                    )
                );
              }

              if (Boolean.TRUE.equals(request.getInStock())) {
                builder.filter(f -> f
                    .range(r -> r.field("stock").gt(co.elastic.clients.json.JsonData.of(0)))
                );
              }

              if (request.getTags() != null && !request.getTags().isEmpty()) {
                for (String tag : request.getTags()) {
                  builder.filter(f -> f
                      .term(t -> t.field("tags").value(tag))
                  );
                }
              }

              return builder;
            })
        )
        .withSort(buildSort(request))
        .withPageable(PageRequest.of(request.getPage(), request.getSize()))
        .build();
  }

  private co.elastic.clients.elasticsearch._types.SortOptions buildSort(ProductSearchRequest request) {
    String sortBy = request.getSortBy();
    boolean isAsc = "asc".equalsIgnoreCase(request.getSortDir());
    SortOrder order = isAsc ? SortOrder.Asc : SortOrder.Desc;

    return switch (sortBy) {
      case "price" -> co.elastic.clients.elasticsearch._types.SortOptions.of(s ->
          s.field(f -> f.field("price").order(order)));
      case "rating" -> co.elastic.clients.elasticsearch._types.SortOptions.of(s ->
          s.field(f -> f.field("averageRating").order(SortOrder.Desc)));
      case "newest" -> co.elastic.clients.elasticsearch._types.SortOptions.of(s ->
          s.field(f -> f.field("createdAt").order(SortOrder.Desc)));
      case "name" -> co.elastic.clients.elasticsearch._types.SortOptions.of(s ->
          s.field(f -> f.field("name.keyword").order(order)));
      default -> co.elastic.clients.elasticsearch._types.SortOptions.of(s ->
          s.score(sc -> sc.order(SortOrder.Desc)));
    };
  }
}