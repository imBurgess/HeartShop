CREATE TABLE IF NOT EXISTS wishlist (
    wishlist_id BIGSERIAL PRIMARY KEY,
    member_id   BIGINT NOT NULL REFERENCES member(member_id) ON DELETE CASCADE,
    product_id  BIGINT NOT NULL REFERENCES product(product_id) ON DELETE CASCADE,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (member_id, product_id)
);
