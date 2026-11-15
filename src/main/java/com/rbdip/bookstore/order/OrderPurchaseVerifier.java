package com.rbdip.bookstore.order;

import com.rbdip.bookstore.review.PurchaseVerifier;
import org.springframework.stereotype.Component;

@Component
public class OrderPurchaseVerifier implements PurchaseVerifier {

    private final OrderItemRepository orderItemRepository;

    public OrderPurchaseVerifier(OrderItemRepository orderItemRepository) {
        this.orderItemRepository = orderItemRepository;
    }

    @Override
    public boolean isProductPurchased(Long productId) {
        return orderItemRepository.existsByProductId(productId);
    }
}
