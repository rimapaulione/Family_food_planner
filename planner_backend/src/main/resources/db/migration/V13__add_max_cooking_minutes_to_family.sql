ALTER TABLE family
    ADD COLUMN max_weekday_cooking_minutes INT
        CHECK (max_weekday_cooking_minutes IS NULL OR max_weekday_cooking_minutes > 0),
    ADD COLUMN max_weekend_cooking_minutes INT
        CHECK (max_weekend_cooking_minutes IS NULL OR max_weekend_cooking_minutes > 0);
