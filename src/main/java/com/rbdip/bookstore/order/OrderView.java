package com.rbdip.bookstore.order;

import java.util.List;

public record OrderView(Long id, String customerFullName, String status, List<ItemView> items) {

    public record ItemView(String productName, Integer quantity) {
    }
}
