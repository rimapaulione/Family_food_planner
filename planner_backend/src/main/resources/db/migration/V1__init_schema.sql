CREATE TABLE category
(
    id   BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE tag
(
    id   BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE ingredient
(
    id         UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    name_lt    VARCHAR(100) NOT NULL UNIQUE,
    unit       VARCHAR(20)  NOT NULL CHECK (unit IN ('VNT', 'G', 'ML')),
    created_at TIMESTAMP    NOT NULL DEFAULT now()
);

CREATE TABLE recipe
(
    id                   UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    name                 VARCHAR(100) NOT NULL UNIQUE,
    category_id          BIGINT       NOT NULL REFERENCES category (id),
    default_serving      SMALLINT     NOT NULL DEFAULT 4 CHECK
        (default_serving > 0 AND default_serving <= 50),
    cooking_time_minutes SMALLINT     NOT NULL CHECK (cooking_time_minutes > 0),
    leftover_recipe_id   UUID         REFERENCES recipe (id) ON DELETE SET NULL,
    is_favorite          BOOLEAN      NOT NULL DEFAULT FALSE,
    notes                VARCHAR(1000),
    created_at           TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at           TIMESTAMP    NOT NULL DEFAULT now()
);

CREATE TABLE recipe_tag
(
    recipe_id UUID   NOT NULL REFERENCES recipe (id) ON DELETE CASCADE,
    tag_id    BIGINT NOT NULL REFERENCES tag (id),
    PRIMARY KEY (recipe_id, tag_id)
);

CREATE TABLE recipe_ingredient
(
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    recipe_id     UUID           NOT NULL REFERENCES recipe (id) ON DELETE CASCADE,
    ingredient_id UUID           NOT NULL REFERENCES ingredient (id),
    quantity      DECIMAL(10, 2) NOT NULL CHECK (quantity > 0),
    UNIQUE (recipe_id, ingredient_id)
);
