ALTER TABLE recipe
    ADD COLUMN family_id UUID REFERENCES family (id) ON DELETE CASCADE;

ALTER TABLE ingredient
    ADD COLUMN family_id UUID REFERENCES family (id) ON DELETE CASCADE;

ALTER TABLE recipe
    DROP CONSTRAINT IF EXISTS recipe_name_key;
ALTER TABLE recipe
    ADD CONSTRAINT recipe_name_family_id_key UNIQUE (name, family_id);

ALTER TABLE ingredient
    DROP CONSTRAINT IF EXISTS ingredient_name_lt_key;
ALTER TABLE ingredient
    ADD CONSTRAINT ingredient_name_lt_family_id_key UNIQUE (name_lt, family_id);
