CREATE TABLE meal_plan
(
    id         UUID PRIMARY KEY     DEFAULT gen_random_uuid(),
    family_id  UUID        NOT NULL REFERENCES family (id) ON DELETE CASCADE,
    start_date DATE        NOT NULL,
    end_date   DATE        NOT NULL,
    status     VARCHAR(20) NOT NULL DEFAULT 'DRAFT'
        CHECK (status IN ('DRAFT', 'LOCKED')),
    created_at TIMESTAMP   NOT NULL DEFAULT now(),
    updated_at TIMESTAMP   NOT NULL DEFAULT now(),
    UNIQUE (family_id, start_date)
);

CREATE TABLE meal_slot
(
    id           UUID PRIMARY KEY     DEFAULT gen_random_uuid(),
    meal_plan_id UUID        NOT NULL REFERENCES meal_plan (id) ON DELETE CASCADE,
    date         DATE        NOT NULL,
    meal_type    VARCHAR(20) NOT NULL CHECK (meal_type IN ('BREAKFAST', 'LUNCH', 'DINNER')),
    recipe_id    UUID REFERENCES recipe (id) ON DELETE SET NULL,
    servings     INTEGER CHECK (servings IS NULL OR (servings >= 1 AND servings <= 50)),
    created_at   TIMESTAMP   NOT NULL DEFAULT now(),
    updated_at   TIMESTAMP   NOT NULL DEFAULT now(),
    UNIQUE (meal_plan_id, date, meal_type)
);

CREATE INDEX idx_meal_slot_plan ON meal_slot (meal_plan_id);
CREATE INDEX idx_meal_slot_recipe ON meal_slot (recipe_id);
