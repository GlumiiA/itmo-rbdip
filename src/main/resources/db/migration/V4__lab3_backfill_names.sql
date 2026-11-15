UPDATE customers
SET first_name = split_part(btrim(full_name), ' ', 1),
    last_name = btrim(substr(btrim(full_name), length(split_part(btrim(full_name), ' ', 1)) + 1))
WHERE full_name IS NOT NULL
  AND (first_name IS NULL OR last_name IS NULL);
