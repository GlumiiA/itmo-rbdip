package com.rbdip.bookstore.review;

import static org.assertj.core.api.Assertions.assertThat;

import com.rbdip.bookstore.order.CreateOrderRequest;
import com.rbdip.bookstore.reference.AbstractIntegrationTest;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class ReviewApiTest extends AbstractIntegrationTest {

    @Test
    void reviewOfOrderedProductIsVerified() {
        Long productId = createProduct("Refactoring");
        CreateOrderRequest order = new CreateOrderRequest(
                "Ivan Petrov", "Moscow", null, "regular", null,
                List.of(new CreateOrderRequest.Item(productId, 1)));
        restTemplate.postForEntity("/orders", order, Map.class);

        ResponseEntity<Map> response = postReview(productId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).containsEntry("verifiedPurchase", true);
    }

    @Test
    void reviewOfNeverOrderedProductIsNotVerified() {
        Long productId = createProduct("Clean Code");

        ResponseEntity<Map> response = postReview(productId);

        assertThat(response.getBody()).containsEntry("verifiedPurchase", false);
    }

    @Test
    void listsReviewsOfProduct() {
        Long productId = createProduct("Domain-Driven Design");
        postReview(productId);

        ResponseEntity<List> response = restTemplate.getForEntity("/products/" + productId + "/reviews", List.class);

        assertThat(response.getBody()).hasSize(1);
        assertThat(((Map<?, ?>) response.getBody().get(0)).get("authorName")).isEqualTo("Anna");
    }

    private ResponseEntity<Map> postReview(Long productId) {
        return restTemplate.postForEntity(
                "/products/" + productId + "/reviews",
                Map.of("authorName", "Anna", "rating", 5, "comment", "good"),
                Map.class);
    }

    private Long createProduct(String name) {
        ResponseEntity<Map> response = restTemplate.postForEntity(
                "/products", Map.of("name", name, "price", new BigDecimal("40.00")), Map.class);
        return Long.valueOf(response.getBody().get("id").toString());
    }
}
