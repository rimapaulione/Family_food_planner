CREATE TABLE shopping_list_manual_history
(
    id         UUID PRIMARY KEY        DEFAULT gen_random_uuid(),
    family_id  UUID           NOT NULL REFERENCES family (id) ON DELETE CASCADE,
    week_start DATE           NOT NULL,
    name       VARCHAR(100)   NOT NULL,
    unit       VARCHAR(20)    NOT NULL CHECK (unit IN ('VNT', 'G', 'ML')),
    quantity   NUMERIC(10, 2) NOT NULL CHECK (quantity >= 0.01),
    is_bought  BOOLEAN        NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP      NOT NULL DEFAULT now()
);

CREATE INDEX idx_shopping_list_manual_history_family_week
    ON shopping_list_manual_history (family_id, week_start, name);