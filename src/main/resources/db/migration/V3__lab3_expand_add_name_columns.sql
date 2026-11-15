ALTER TABLE customers
    ADD COLUMN first_name VARCHAR(255),
    ADD COLUMN last_name VARCHAR(255);

ALTER TABLE customers ALTER COLUMN full_name DROP NOT NULL;

CREATE FUNCTION customers_sync_names() RETURNS trigger AS $$
BEGIN
    IF NEW.full_name IS NOT NULL AND NEW.first_name IS NULL AND NEW.last_name IS NULL THEN
        NEW.first_name := split_part(btrim(NEW.full_name), ' ', 1);
        NEW.last_name := btrim(substr(btrim(NEW.full_name), length(NEW.first_name) + 1));
    ELSIF NEW.full_name IS NULL AND NEW.first_name IS NOT NULL THEN
        NEW.full_name := btrim(NEW.first_name || ' ' || COALESCE(NEW.last_name, ''));
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER customers_sync_names_trigger
    BEFORE INSERT OR UPDATE ON customers
    FOR EACH ROW EXECUTE FUNCTION customers_sync_names();
