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
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderNotifier notifier;

    public OrderService(
            OrderRequestValidator validator,
            OrderLineResolver lineResolver,
            PricingCalculator pricingCalculator,
            OrderRepository orderRepository,
            OrderItemRepository orderItemRepository,
            OrderNotifier notifier) {
        this.validator = validator;
        this.lineResolver = lineResolver;
        this.pricingCalculator = pricingCalculator;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.notifier = notifier;
    }

    @Transactional
    public Order createOrder(CreateOrderRequest request) {
        validator.validate(request);
        List<OrderLine> lines = lineResolver.resolve(request.items());
        BigDecimal total = calculateTotal(request, lines);
        Order order = saveOrder(request, lines);
        notifier.orderPlaced(request.customerFullName(), order.getId(), total);
        return order;
    }

    private BigDecimal calculateTotal(CreateOrderRequest request, List<OrderLine> lines) {
        List<PricingCalculator.LineItem> lineItems = lines.stream()
                .map(line -> new PricingCalculator.LineItem(line.product().getPrice(), line.quantity()))
                .toList();
        return pricingCalculator.calculateOrderTotal(lineItems, request.customerType(), request.couponCode());
    }

    private Order saveOrder(CreateOrderRequest request, List<OrderLine> lines) {
        Order order = orderRepository.save(new Order(
                request.customerFullName(), request.customerAddress(), request.customerPhone(), Order.STATUS_NEW));
        for (OrderLine line : lines) {
            orderItemRepository.save(new OrderItem(
                    order.getId(), line.product().getName(), line.product().getPrice(), line.quantity()));
        }
        return order;
    }
}
