CREATE TABLE shopping_list_plan_history
(
    id            UUID PRIMARY KEY        DEFAULT gen_random_uuid(),
    family_id     UUID           NOT NULL REFERENCES family (id) ON DELETE CASCADE,
    week_start    DATE           NOT NULL,
    ingredient_id UUID           NOT NULL REFERENCES ingredient (id) ON DELETE CASCADE,
    quantity      NUMERIC(10, 2) NOT NULL CHECK (quantity > 0),
    created_at    TIMESTAMP      NOT NULL DEFAULT now(),
    UNIQUE (family_id, week_start, ingredient_id)
);

CREATE INDEX idx_shopping_list_plan_history_family_week
    ON shopping_list_plan_history (family_id, week_start);
