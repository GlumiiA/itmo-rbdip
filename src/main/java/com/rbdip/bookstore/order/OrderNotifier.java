package com.rbdip.bookstore.order;

import java.math.BigDecimal;

public interface OrderNotifier {

    void orderPlaced(String customerName, Long orderId, BigDecimal total);
}
