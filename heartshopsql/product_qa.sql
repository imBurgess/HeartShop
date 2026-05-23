CREATE TABLE IF NOT EXISTS product_qa (
    qa_id       BIGSERIAL PRIMARY KEY,
    product_id  BIGINT NOT NULL,
    member_id   BIGINT,
    question    TEXT NOT NULL,
    answer      TEXT,
    is_public   BOOLEAN NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    answered_at TIMESTAMP,
    FOREIGN KEY (product_id) REFERENCES product(product_id) ON DELETE CASCADE,
    FOREIGN KEY (member_id)  REFERENCES member(member_id)  ON DELETE SET NULL
);
