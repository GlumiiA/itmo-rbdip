CREATE TABLE customers (
    id BIGSERIAL PRIMARY KEY,
    full_name VARCHAR(255) NOT NULL,
    address VARCHAR(500),
    phone VARCHAR(50)
);

INSERT INTO customers (full_name, address, phone)
SELECT DISTINCT customer_full_name, customer_address, customer_phone
FROM orders;

ALTER TABLE orders ADD COLUMN customer_id BIGINT REFERENCES customers (id);

UPDATE orders o
SET customer_id = c.id
FROM customers c
WHERE c.full_name = o.customer_full_name
  AND c.address IS NOT DISTINCT FROM o.customer_address
  AND c.phone IS NOT DISTINCT FROM o.customer_phone;

ALTER TABLE orders ALTER COLUMN customer_id SET NOT NULL;

ALTER TABLE orders
    DROP COLUMN customer_full_name,
    DROP COLUMN customer_address,
    DROP COLUMN customer_phone;

CREATE INDEX idx_orders_customer_id ON orders (customer_id);

INSERT INTO products (name, price)
SELECT DISTINCT ON (oi.product_name) oi.product_name, oi.product_price
FROM order_items oi
WHERE NOT EXISTS (SELECT 1 FROM products p WHERE p.name = oi.product_name)
ORDER BY oi.product_name, oi.id;

ALTER TABLE order_items ADD COLUMN product_id BIGINT REFERENCES products (id);

UPDATE order_items oi
SET product_id = (SELECT MIN(p.id) FROM products p WHERE p.name = oi.product_name);

ALTER TABLE order_items ALTER COLUMN product_id SET NOT NULL;

ALTER TABLE order_items
    DROP COLUMN product_name,
    DROP COLUMN product_price;

CREATE INDEX idx_order_items_order_id ON order_items (order_id);
CREATE INDEX idx_order_items_product_id ON order_items (product_id);
