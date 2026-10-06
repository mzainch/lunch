ALTER TABLE days DROP CONSTRAINT IF EXISTS days_date_key;
ALTER TABLE days ALTER COLUMN bill_minor TYPE INTEGER USING bill_minor / 100;
ALTER TABLE friends ALTER COLUMN starting_amount_minor TYPE INTEGER USING starting_amount_minor / 100;
ALTER TABLE settings ALTER COLUMN pool_minor TYPE INTEGER USING pool_minor / 100;
ALTER TABLE dues ALTER COLUMN amount_minor TYPE INTEGER USING amount_minor / 100;