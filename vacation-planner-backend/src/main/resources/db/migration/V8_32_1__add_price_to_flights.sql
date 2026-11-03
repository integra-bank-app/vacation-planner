ALTER TABLE flights ADD COLUMN price numeric(10,2);
UPDATE flights SET price = 100.00;
ALTER TABLE flights ALTER COLUMN price SET NOT NULL;
