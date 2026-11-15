package com.rbdip.bookstore.order;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class PricingCalculator {

    private static final String VIP = "vip";
    private static final String WHOLESALE = "wholesale";
    private static final String COUPON_FIXED = "SAVE10";
    private static final String COUPON_PERCENT = "SAVE20PERCENT";

    private static final int BULK_QUANTITY_THRESHOLD = 10;
    private static final BigDecimal BULK_MULTIPLIER = new BigDecimal("0.95");
    private static final BigDecimal VIP_MULTIPLIER = new BigDecimal("0.9");
    private static final BigDecimal WHOLESALE_MULTIPLIER = new BigDecimal("0.85");
    private static final BigDecimal FIXED_COUPON_AMOUNT = BigDecimal.TEN;
    private static final BigDecimal PERCENT_COUPON_MULTIPLIER = new BigDecimal("0.8");
    private static final BigDecimal LARGE_ORDER_THRESHOLD = new BigDecimal("1000");
    private static final BigDecimal LARGE_ORDER_MULTIPLIER = new BigDecimal("0.98");
    private static final int PRICE_SCALE = 2;

    public record LineItem(BigDecimal price, int quantity) {
    }

    public BigDecimal calculateOrderTotal(List<LineItem> items, String customerType, String couponCode) {
        BigDecimal total = sumLines(items);
        total = applyCustomerDiscount(total, customerType);
        total = applyCoupon(total, couponCode);
        total = total.max(BigDecimal.ZERO);
        total = applyLargeOrderDiscount(total);
        return total.setScale(PRICE_SCALE, RoundingMode.HALF_UP);
    }

    private BigDecimal sumLines(List<LineItem> items) {
        return items.stream().map(this::linePrice).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal linePrice(LineItem item) {
        BigDecimal price = item.price().multiply(BigDecimal.valueOf(item.quantity()));
        return item.quantity() > BULK_QUANTITY_THRESHOLD ? price.multiply(BULK_MULTIPLIER) : price;
    }

    private BigDecimal applyCustomerDiscount(BigDecimal total, String customerType) {
        return switch (customerType == null ? "" : customerType) {
            case VIP -> total.multiply(VIP_MULTIPLIER);
            case WHOLESALE -> total.multiply(WHOLESALE_MULTIPLIER);
            default -> total;
        };
    }

    private BigDecimal applyCoupon(BigDecimal total, String couponCode) {
        return switch (couponCode == null ? "" : couponCode) {
            case COUPON_FIXED -> total.subtract(FIXED_COUPON_AMOUNT);
            case COUPON_PERCENT -> total.multiply(PERCENT_COUPON_MULTIPLIER);
            default -> total;
        };
    }

    private BigDecimal applyLargeOrderDiscount(BigDecimal total) {
        return total.compareTo(LARGE_ORDER_THRESHOLD) > 0 ? total.multiply(LARGE_ORDER_MULTIPLIER) : total;
    }
}
