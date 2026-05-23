CREATE TABLE IF NOT EXISTS notification (
    notification_id BIGSERIAL PRIMARY KEY,
    member_id       BIGINT NOT NULL,
    type            VARCHAR(50) NOT NULL DEFAULT 'QA_REPLY',
    title           VARCHAR(200) NOT NULL,
    content         TEXT NOT NULL,
    link_url        VARCHAR(500),
    is_read         BOOLEAN NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (member_id) REFERENCES member(member_id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_notification_member_id ON notification(member_id);
CREATE INDEX IF NOT EXISTS idx_notification_is_read ON notification(member_id, is_read);
