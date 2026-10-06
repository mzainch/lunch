ALTER TABLE friends ADD COLUMN starting_amount_minor BIGINT NOT NULL DEFAULT 0 CHECK (starting_amount_minor >= 0);
CREATE TABLE dues (
    id BIGSERIAL PRIMARY KEY,
    month DATE NOT NULL,
    friend_id BIGINT NOT NULL REFERENCES friends(id) ON DELETE CASCADE,
    amount_minor BIGINT NOT NULL CHECK (amount_minor >= 0),
    CONSTRAINT uk_dues_month_friend UNIQUE (month, friend_id)
);