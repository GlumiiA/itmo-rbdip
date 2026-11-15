package com.rbdip.bookstore.review;

import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ReviewService {

    private static final String ANONYMOUS_AUTHOR = "anonymous";

    private final ReviewRepository reviewRepository;
    private final PurchaseVerifier purchaseVerifier;

    public ReviewService(ReviewRepository reviewRepository, PurchaseVerifier purchaseVerifier) {
        this.reviewRepository = reviewRepository;
        this.purchaseVerifier = purchaseVerifier;
    }

    public AddedReview addReview(Long productId, String authorName, Integer rating, String comment) {
        boolean verifiedPurchase = purchaseVerifier.isProductPurchased(productId);
        Review review = new Review(productId, authorName == null ? ANONYMOUS_AUTHOR : authorName, rating, comment);
        return new AddedReview(reviewRepository.save(review), verifiedPurchase);
    }

    public List<Review> listReviews(Long productId) {
        return reviewRepository.findByProductId(productId);
    }
}
