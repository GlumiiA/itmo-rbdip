DROP TRIGGER customers_sync_names_trigger ON customers;
DROP FUNCTION customers_sync_names();

ALTER TABLE customers
    ALTER COLUMN first_name SET NOT NULL,
    ALTER COLUMN last_name SET NOT NULL;

ALTER TABLE customers DROP COLUMN full_name;
