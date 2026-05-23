-- ============================================================
--  HeartShop 資料庫建表腳本 (PostgreSQL)
--  建立順序已依外鍵依賴排序，直接整份執行即可
-- ============================================================

-- ── 1. 會員 ─────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS member (
    member_id       BIGSERIAL PRIMARY KEY,
    email           VARCHAR(255) UNIQUE NOT NULL,
    password        VARCHAR(255)        NOT NULL,
    name            VARCHAR(255),
    phone           VARCHAR(20),
    birthday        DATE,
    address         TEXT,
    role            VARCHAR(50)  DEFAULT 'CUSTOMER',   -- CUSTOMER | VIP | ADMIN
    status          VARCHAR(50)  DEFAULT 'ACTIVE',     -- ACTIVE | INACTIVE
    subscribe_edm   BOOLEAN      DEFAULT FALSE,
    bonus_points    INTEGER      DEFAULT 0,
    total_orders    INTEGER      DEFAULT 0,
    total_spent     NUMERIC(10,2) DEFAULT 0.00,
    created_at      TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- ── 2. 商品分類 ──────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS category (
    category_id  BIGSERIAL PRIMARY KEY,
    slug         VARCHAR(255),
    name_zh      VARCHAR(255),
    name_en      VARCHAR(255),
    parent_id    BIGINT REFERENCES category(category_id) ON DELETE SET NULL,
    sort_order   INTEGER  DEFAULT 0,
    is_active    BOOLEAN  DEFAULT TRUE,
    banner_url   VARCHAR(512),
    created_at   TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- ── 3. 商品 ─────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS product (
    product_id              BIGSERIAL PRIMARY KEY,
    category_id             BIGINT       NOT NULL REFERENCES category(category_id),
    code                    VARCHAR(255) UNIQUE,
    name                    VARCHAR(255),
    name_en                 VARCHAR(255),
    price                   NUMERIC(10,2),
    discount_price          NUMERIC(10,2),
    description             TEXT,
    size_info               VARCHAR(255),
    tags                    TEXT,
    is_new                  BOOLEAN  DEFAULT FALSE,
    is_sold_out             BOOLEAN  DEFAULT FALSE,
    is_active               BOOLEAN  DEFAULT TRUE,
    view_count              INTEGER  DEFAULT 0,
    sort_order              INTEGER  DEFAULT 0,
    stock_quantity          INTEGER  DEFAULT 0,
    stock_alert_threshold   INTEGER  DEFAULT 10,
    last_stock_update_at    TIMESTAMP WITH TIME ZONE,
    created_at              TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at              TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- ── 4. 商品圖片 ──────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS product_image (
    image_id    BIGSERIAL PRIMARY KEY,
    product_id  BIGINT       NOT NULL REFERENCES product(product_id) ON DELETE CASCADE,
    image_url   VARCHAR(512),
    sort_order  INTEGER  DEFAULT 0,
    created_at  TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- ── 5. 購物車 ────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS cart_item (
    cart_item_id  BIGSERIAL PRIMARY KEY,
    member_id     BIGINT  NOT NULL REFERENCES member(member_id)  ON DELETE CASCADE,
    product_id    BIGINT  NOT NULL REFERENCES product(product_id) ON DELETE CASCADE,
    size_name     VARCHAR(100),
    quantity      INTEGER NOT NULL DEFAULT 1,
    created_at    TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- ── 6. 訂單主檔 ──────────────────────────────────────────────
-- 使用雙引號避免與 SQL 保留字衝突
CREATE TABLE IF NOT EXISTS "orders" (
    order_id          BIGSERIAL PRIMARY KEY,
    order_no          VARCHAR(50) UNIQUE NOT NULL,
    member_id         BIGINT NOT NULL REFERENCES member(member_id),
    order_date        DATE,
    ship_date         DATE,
    status            VARCHAR(50),        -- pending | PAID | FAILED | COMPLETED | CANCELLED
    payment_method    VARCHAR(100),
    shipping_method   VARCHAR(100),
    receiver_name     VARCHAR(255),
    receiver_phone    VARCHAR(20),
    receiver_address  TEXT,
    subtotal_amount   INTEGER,
    shipping_fee      INTEGER,
    discount_amount   INTEGER,
    bonus_used        INTEGER,
    total_amount      INTEGER,
    remark            TEXT,
    created_at        TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- ── 7. 訂單明細 ──────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS "order_item" (
    order_item_id   BIGSERIAL PRIMARY KEY,
    order_id        BIGINT NOT NULL REFERENCES "orders"(order_id) ON DELETE CASCADE,
    product_id      BIGINT NOT NULL REFERENCES product(product_id),
    product_code    VARCHAR(255),
    product_name    VARCHAR(255),
    product_image   VARCHAR(512),
    size_name       VARCHAR(100),
    unit_price      INTEGER,
    quantity        INTEGER NOT NULL,
    subtotal        INTEGER,
    created_at      TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- ── 8. 收藏清單 ──────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS wishlist (
    wishlist_id  BIGSERIAL PRIMARY KEY,
    member_id    BIGINT NOT NULL REFERENCES member(member_id)  ON DELETE CASCADE,
    product_id   BIGINT NOT NULL REFERENCES product(product_id) ON DELETE CASCADE,
    created_at   TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (member_id, product_id)
);

-- ── 9. 首頁區塊 ──────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS home_block (
    block_id    BIGSERIAL PRIMARY KEY,
    type        VARCHAR(50),    -- CAROUSEL | MEMBER_BANNER | PRODUCT_RECOMMEND | GENERAL_ANNOUNCEMENT
    title       VARCHAR(255),
    subtitle    VARCHAR(255),
    image_url   VARCHAR(512),
    link_url    VARCHAR(512),
    sort_order  INTEGER  DEFAULT 0,
    is_active   BOOLEAN  DEFAULT TRUE,
    start_time  TIMESTAMP WITH TIME ZONE,
    end_time    TIMESTAMP WITH TIME ZONE,
    created_at  TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- ── 10. 首頁區塊關聯商品 ─────────────────────────────────────
CREATE TABLE IF NOT EXISTS home_block_product (
    id          BIGSERIAL PRIMARY KEY,
    block_id    BIGINT NOT NULL REFERENCES home_block(block_id) ON DELETE CASCADE,
    product_id  BIGINT NOT NULL REFERENCES product(product_id)  ON DELETE CASCADE,
    sort_order  INTEGER DEFAULT 0,  -- 0=主推, 1/2=副推
    created_at  TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- ── 11. 庫存異動記錄 ─────────────────────────────────────────
CREATE TABLE IF NOT EXISTS inventory_log (
    log_id            BIGSERIAL PRIMARY KEY,
    product_id        BIGINT NOT NULL REFERENCES product(product_id) ON DELETE CASCADE,
    change_type       VARCHAR(50),   -- ADJUST | SALE | RETURN | STOCK_IN
    quantity_before   INTEGER,
    quantity_change   INTEGER,
    quantity_after    INTEGER,
    operator          VARCHAR(255),
    remark            TEXT,
    created_at        TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- ── 12. 商品問答 ─────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS product_qa (
    qa_id       BIGSERIAL PRIMARY KEY,
    product_id  BIGINT NOT NULL REFERENCES product(product_id) ON DELETE CASCADE,
    member_id   BIGINT          REFERENCES member(member_id)   ON DELETE SET NULL,
    question    TEXT    NOT NULL,
    answer      TEXT,
    is_public   BOOLEAN NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    answered_at TIMESTAMP
);

-- ── 13. 訂單問答 ─────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS order_qa (
    qa_id       BIGSERIAL PRIMARY KEY,
    order_no    VARCHAR(50) NOT NULL,   -- 對應 orders.order_no
    member_id   BIGINT REFERENCES member(member_id) ON DELETE SET NULL,
    question    TEXT    NOT NULL,
    answer      TEXT,
    is_public   BOOLEAN NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    answered_at TIMESTAMP
);

-- ============================================================
--  索引（加速常用查詢）
-- ============================================================
CREATE INDEX IF NOT EXISTS idx_product_category   ON product(category_id);
CREATE INDEX IF NOT EXISTS idx_product_is_active  ON product(is_active);
CREATE INDEX IF NOT EXISTS idx_product_image_pid  ON product_image(product_id);
CREATE INDEX IF NOT EXISTS idx_cart_member        ON cart_item(member_id);
CREATE INDEX IF NOT EXISTS idx_orders_member      ON "orders"(member_id);
CREATE INDEX IF NOT EXISTS idx_orders_status      ON "orders"(status);
CREATE INDEX IF NOT EXISTS idx_order_item_order   ON "order_item"(order_id);
CREATE INDEX IF NOT EXISTS idx_wishlist_member    ON wishlist(member_id);
CREATE INDEX IF NOT EXISTS idx_inventory_product  ON inventory_log(product_id);
CREATE INDEX IF NOT EXISTS idx_product_qa_product ON product_qa(product_id);
CREATE INDEX IF NOT EXISTS idx_product_qa_member  ON product_qa(member_id);
CREATE INDEX IF NOT EXISTS idx_order_qa_order_no  ON order_qa(order_no);
