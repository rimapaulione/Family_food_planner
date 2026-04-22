CREATE TABLE app_user
(
    id            UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    email         VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(60),
    display_name  VARCHAR(100) NOT NULL,
    avatar_url    VARCHAR(500),
    auth_provider VARCHAR(20)  NOT NULL CHECK (auth_provider IN ('LOCAL', 'GOOGLE')),
    role          VARCHAR(20)  NOT NULL CHECK (role IN ('USER', 'ADMIN')),
    created_at    TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at    TIMESTAMP    NOT NULL DEFAULT now()
);

