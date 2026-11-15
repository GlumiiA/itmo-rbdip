package com.rbdip.bookstore.order;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderQueryService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    public OrderQueryService(OrderRepository orderRepository, OrderItemRepository orderItemRepository) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
    }

    @Transactional(readOnly = true)
    public List<OrderView> listOrders() {
        List<Order> orders = orderRepository.findAllWithCustomer();
        if (orders.isEmpty()) {
            return List.of();
        }
        List<Long> orderIds = orders.stream().map(Order::getId).toList();
        Map<Long, List<OrderItem>> itemsByOrder = orderItemRepository.findAllWithProductByOrderIdIn(orderIds).stream()
                .collect(Collectors.groupingBy(OrderItem::getOrderId));
        return orders.stream()
                .map(order -> toView(order, itemsByOrder.getOrDefault(order.getId(), List.of())))
                .toList();
    }

    private OrderView toView(Order order, List<OrderItem> items) {
        List<OrderView.ItemView> itemViews = items.stream()
                .map(item -> new OrderView.ItemView(item.getProductName(), item.getQuantity()))
                .toList();
        return new OrderView(order.getId(), order.getCustomerFullName(), order.getStatus(), itemViews);
    }
}
