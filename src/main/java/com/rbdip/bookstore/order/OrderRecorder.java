package com.rbdip.bookstore.order;

import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class OrderRecorder {

    private final CustomerRepository customerRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    public OrderRecorder(
            CustomerRepository customerRepository,
            OrderRepository orderRepository,
            OrderItemRepository orderItemRepository) {
        this.customerRepository = customerRepository;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
    }

    public Order record(CreateOrderRequest request, List<OrderLine> lines) {
        Customer customer = customerRepository.save(
                new Customer(request.customerFullName(), request.customerAddress(), request.customerPhone()));
        Order order = orderRepository.save(new Order(customer, Order.STATUS_NEW));
        for (OrderLine line : lines) {
            orderItemRepository.save(new OrderItem(order.getId(), line.product(), line.quantity()));
        }
        return order;
    }
}
