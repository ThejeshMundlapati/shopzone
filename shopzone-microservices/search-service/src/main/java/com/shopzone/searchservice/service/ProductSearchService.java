package com.shopzone.searchservice.service;

import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import com.shopzone.searchservice.model.ProductDocument;
import com.shopzone.searchservice.repository.ProductSearchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collectors;

@Service @RequiredArgsConstructor @Slf4j
public class ProductSearchService {
    private final ProductSearchRepository searchRepository;
    private final ElasticsearchOperations esOps;

    public Map<String, Object> search(String query, Double minPrice, Double maxPrice, String categoryId,
            String brand, Double minRating, Boolean inStock, int page, int size, String sortBy, String sortDir) {
        long start = System.currentTimeMillis();

        NativeQuery nq = NativeQuery.builder()
            .withQuery(q -> q.bool(b -> {
                BoolQuery.Builder builder = b;
                if (query != null && !query.isBlank())
                    builder.must(m -> m.multiMatch(mm -> mm.query(query).fields("name^3","description^2","brand^2","tags").fuzziness("AUTO")));
                builder.filter(f -> f.term(t -> t.field("active").value(true)));
                if (minPrice != null || maxPrice != null)
                    builder.filter(f -> f.range(r -> { var rng = r.field("price");
                        if (minPrice != null) rng.gte(co.elastic.clients.json.JsonData.of(minPrice));
                        if (maxPrice != null) rng.lte(co.elastic.clients.json.JsonData.of(maxPrice)); return rng; }));
                if (categoryId != null && !categoryId.isBlank())
                    builder.filter(f -> f.term(t -> t.field("categoryId").value(categoryId)));
                if (brand != null && !brand.isBlank())
                    builder.filter(f -> f.wildcard(w -> w.field("brand").wildcard("*"+brand+"*").caseInsensitive(true)));
                if (minRating != null)
                    builder.filter(f -> f.range(r -> r.field("averageRating").gte(co.elastic.clients.json.JsonData.of(minRating))));
                if (Boolean.TRUE.equals(inStock))
                    builder.filter(f -> f.range(r -> r.field("stock").gt(co.elastic.clients.json.JsonData.of(0))));
                return builder;
            }))
            .withSort(buildSort(sortBy, sortDir))
            .withPageable(PageRequest.of(page, Math.min(size, 100)))
            .build();

        SearchHits<ProductDocument> hits = esOps.search(nq, ProductDocument.class);
        List<Map<String, Object>> products = hits.getSearchHits().stream().map(h -> {
            ProductDocument d = h.getContent();
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", d.getId()); m.put("name", d.getName()); m.put("slug", d.getSlug());
            m.put("brand", d.getBrand()); m.put("price", d.getPrice()); m.put("salePrice", d.getSalePrice());
            m.put("stock", d.getStock()); m.put("images", d.getImages()); m.put("categoryId", d.getCategoryId());
            m.put("averageRating", d.getAverageRating()); m.put("reviewCount", d.getReviewCount());
            m.put("score", h.getScore());
            return m;
        }).collect(Collectors.toList());

        return Map.of("products", products, "totalHits", hits.getTotalHits(),
            "totalPages", (int) Math.ceil((double) hits.getTotalHits() / size),
            "currentPage", page, "searchTimeMs", System.currentTimeMillis() - start, "query", query != null ? query : "");
    }

    public void syncProduct(ProductDocument doc) { searchRepository.save(doc); }
    public void removeProduct(String id) { searchRepository.deleteById(id); }
    public void syncAll(List<ProductDocument> docs) { searchRepository.deleteAll(); searchRepository.saveAll(docs); }
    public long count() { return searchRepository.count(); }

    public void updateRating(String id, Double rating, Integer count) {
        searchRepository.findById(id).ifPresent(d -> {
            d.setAverageRating(rating != null ? rating : 0.0);
            d.setReviewCount(count != null ? count : 0);
            searchRepository.save(d);
        });
    }

    private co.elastic.clients.elasticsearch._types.SortOptions buildSort(String sortBy, String dir) {
        boolean asc = "asc".equalsIgnoreCase(dir);
        SortOrder order = asc ? SortOrder.Asc : SortOrder.Desc;
        return switch (sortBy != null ? sortBy : "relevance") {
            case "price" -> co.elastic.clients.elasticsearch._types.SortOptions.of(s -> s.field(f -> f.field("price").order(order)));
            case "rating" -> co.elastic.clients.elasticsearch._types.SortOptions.of(s -> s.field(f -> f.field("averageRating").order(order)));
            case "newest" -> co.elastic.clients.elasticsearch._types.SortOptions.of(s -> s.field(f -> f.field("createdAt").order(order)));
            default -> co.elastic.clients.elasticsearch._types.SortOptions.of(s -> s.score(sc -> sc.order(SortOrder.Desc)));
        };
    }
}
