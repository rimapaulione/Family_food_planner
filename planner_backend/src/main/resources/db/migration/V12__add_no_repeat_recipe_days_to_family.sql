ALTER TABLE family
    ADD COLUMN no_repeat_recipe_days INT NOT NULL DEFAULT 14
        CHECK (no_repeat_recipe_days >= 0);
