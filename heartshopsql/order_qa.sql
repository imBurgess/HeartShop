CREATE TABLE IF NOT EXISTS order_qa (
    qa_id      BIGSERIAL PRIMARY KEY,
    order_no   VARCHAR(50) NOT NULL,
    member_id  BIGINT,
    question   TEXT NOT NULL,
    answer     TEXT,
    is_public  BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    answered_at TIMESTAMP,
    FOREIGN KEY (member_id) REFERENCES member(member_id) ON DELETE SET NULL
);
