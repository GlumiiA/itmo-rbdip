package com.rbdip.bookstore.order;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderService {

    private final OrderRequestValidator validator;
    private final OrderLineResolver lineResolver;
    private final PricingCalculator pricingCalculator;
    private final OrderRecorder recorder;
    private final OrderNotifier notifier;

    public OrderService(
            OrderRequestValidator validator,
            OrderLineResolver lineResolver,
            PricingCalculator pricingCalculator,
            OrderRecorder recorder,
            OrderNotifier notifier) {
        this.validator = validator;
        this.lineResolver = lineResolver;
        this.pricingCalculator = pricingCalculator;
        this.recorder = recorder;
        this.notifier = notifier;
    }

    @Transactional
    public Order createOrder(CreateOrderRequest request) {
        validator.validate(request);
        List<OrderLine> lines = lineResolver.resolve(request.items());
        BigDecimal total = calculateTotal(request, lines);
        Order order = recorder.record(request, lines);
        notifier.orderPlaced(request.customerFullName(), order.getId(), total);
        return order;
    }

    private BigDecimal calculateTotal(CreateOrderRequest request, List<OrderLine> lines) {
        List<PricingCalculator.LineItem> lineItems = lines.stream()
                .map(line -> new PricingCalculator.LineItem(line.product().getPrice(), line.quantity()))
                .toList();
        return pricingCalculator.calculateOrderTotal(lineItems, request.customerType(), request.couponCode());
    }
}
