CREATE TABLE ingredient_template
(
    id      UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    name_lt VARCHAR(100) NOT NULL UNIQUE,
    unit    VARCHAR(20)  NOT NULL CHECK (unit IN ('VNT', 'G', 'ML'))
);


INSERT INTO ingredient_template (name_lt, unit)
SELECT name_lt, unit
FROM ingredient
WHERE family_id IS NULL;

DELETE FROM recipe_ingredient
WHERE ingredient_id IN (SELECT id FROM ingredient WHERE family_id IS NULL);


DELETE FROM recipe WHERE family_id IS NULL;


DELETE FROM ingredient WHERE family_id IS NULL;
