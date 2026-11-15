package com.rbdip.bookstore.review;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;

class ReviewServiceTest {

    private static final Long PRODUCT_ID = 7L;

    private final ReviewRepository reviewRepository = mock(ReviewRepository.class);
    private final PurchaseVerifier purchaseVerifier = mock(PurchaseVerifier.class);
    private final ReviewService service = new ReviewService(reviewRepository, purchaseVerifier);

    ReviewServiceTest() {
        when(reviewRepository.save(any(Review.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void marksReviewOfPurchasedProductAsVerified() {
        when(purchaseVerifier.isProductPurchased(PRODUCT_ID)).thenReturn(true);

        AddedReview added = service.addReview(PRODUCT_ID, "Ivan", 5, "great");

        assertThat(added.verifiedPurchase()).isTrue();
        assertThat(added.review().getAuthorName()).isEqualTo("Ivan");
        assertThat(added.review().getProductId()).isEqualTo(PRODUCT_ID);
    }

    @Test
    void doesNotMarkReviewOfUnpurchasedProductAsVerified() {
        when(purchaseVerifier.isProductPurchased(PRODUCT_ID)).thenReturn(false);

        assertThat(service.addReview(PRODUCT_ID, "Ivan", 4, null).verifiedPurchase()).isFalse();
    }

    @Test
    void missingAuthorBecomesAnonymous() {
        AddedReview added = service.addReview(PRODUCT_ID, null, 3, "ok");

        assertThat(added.review().getAuthorName()).isEqualTo("anonymous");
    }
}
