package com.rbdip.bookstore.order;

import static org.assertj.core.api.Assertions.assertThat;

import com.rbdip.bookstore.order.PricingCalculator.LineItem;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

class PricingCalculatorCharacterizationTest {

    private final PricingCalculator calculator = new PricingCalculator();

    private static LineItem line(String price, int quantity) {
        return new LineItem(new BigDecimal(price), quantity);
    }

    private BigDecimal total(String customerType, String coupon, LineItem... items) {
        return calculator.calculateOrderTotal(List.of(items), customerType, coupon);
    }

    @Test
    void emptyOrderCostsNothing() {
        assertThat(calculator.calculateOrderTotal(List.of(), "regular", null)).isEqualTo(new BigDecimal("0.00"));
    }

    @Test
    void sumsSeveralLines() {
        assertThat(total("regular", null, line("10.00", 2), line("5.00", 3))).isEqualTo(new BigDecimal("35.00"));
    }

    @Test
    void tenItemsOfOneLineHaveNoBulkDiscount() {
        assertThat(total("regular", null, line("10.00", 10))).isEqualTo(new BigDecimal("100.00"));
    }

    @Test
    void elevenItemsOfOneLineGetFivePercentOff() {
        assertThat(total("regular", null, line("10.00", 11))).isEqualTo(new BigDecimal("104.50"));
    }

    @Test
    void bulkDiscountAppliesOnlyToTheLargeLine() {
        assertThat(total("regular", null, line("10.00", 11), line("10.00", 1)))
                .isEqualTo(new BigDecimal("114.50"));
    }

    @Test
    void vipCustomerGetsTenPercentOff() {
        assertThat(total("vip", null, line("100.00", 1))).isEqualTo(new BigDecimal("90.00"));
    }

    @Test
    void wholesaleCustomerGetsFifteenPercentOff() {
        assertThat(total("wholesale", null, line("100.00", 1))).isEqualTo(new BigDecimal("85.00"));
    }

    @Test
    void unknownCustomerTypePaysFullPrice() {
        assertThat(total("gold", null, line("100.00", 1))).isEqualTo(new BigDecimal("100.00"));
    }

    @Test
    void nullCustomerTypePaysFullPrice() {
        assertThat(total(null, null, line("100.00", 1))).isEqualTo(new BigDecimal("100.00"));
    }

    @Test
    void fixedCouponTakesTenOff() {
        assertThat(total("regular", "SAVE10", line("100.00", 1))).isEqualTo(new BigDecimal("90.00"));
    }

    @Test
    void percentCouponTakesTwentyPercentOff() {
        assertThat(total("regular", "SAVE20PERCENT", line("100.00", 1))).isEqualTo(new BigDecimal("80.00"));
    }

    @Test
    void unknownCouponIsIgnored() {
        assertThat(total("regular", "FREE", line("100.00", 1))).isEqualTo(new BigDecimal("100.00"));
    }

    @Test
    void couponCodeIsCaseSensitive() {
        assertThat(total("regular", "save10", line("100.00", 1))).isEqualTo(new BigDecimal("100.00"));
    }

    @Test
    void customerDiscountIsAppliedBeforeFixedCoupon() {
        assertThat(total("vip", "SAVE10", line("100.00", 1))).isEqualTo(new BigDecimal("80.00"));
    }

    @Test
    void allDiscountsStack() {
        assertThat(total("vip", "SAVE20PERCENT", line("10.00", 20))).isEqualTo(new BigDecimal("136.80"));
    }

    @Test
    void fixedCouponNeverMakesTotalNegative() {
        assertThat(total("regular", "SAVE10", line("5.00", 1))).isEqualTo(new BigDecimal("0.00"));
    }

    @Test
    void totalExactlyAtThresholdHasNoLargeOrderDiscount() {
        assertThat(total("regular", null, line("1000.00", 1))).isEqualTo(new BigDecimal("1000.00"));
    }

    @Test
    void totalJustAboveThresholdGetsTwoPercentOff() {
        assertThat(total("regular", null, line("1000.01", 1))).isEqualTo(new BigDecimal("980.01"));
    }

    @Test
    void largeOrderDiscountIsAppliedAfterOtherDiscounts() {
        assertThat(total("vip", null, line("1500.00", 1))).isEqualTo(new BigDecimal("1323.00"));
    }

    @Test
    void resultIsRoundedHalfUpToCents() {
        assertThat(total("regular", null, line("0.125", 1))).isEqualTo(new BigDecimal("0.13"));
        assertThat(total("regular", null, line("0.124", 1))).isEqualTo(new BigDecimal("0.12"));
    }

    @Test
    void resultAlwaysHasTwoDecimals() {
        assertThat(total("regular", null, line("7", 1)).scale()).isEqualTo(2);
    }
}
