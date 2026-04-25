CREATE TABLE family
(
    id                 UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    name               VARCHAR(100) NOT NULL,
    created_by         UUID         NOT NULL REFERENCES app_user (id),
    is_setup_completed BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at         TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at         TIMESTAMP    NOT NULL DEFAULT now()
);

CREATE TABLE family_invitation
(
    id            UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    family_id     UUID         NOT NULL REFERENCES family (id),
    invited_email VARCHAR(255) NOT NULL,
    invited_by    UUID         NOT NULL REFERENCES app_user (id),
    token         VARCHAR(64)  NOT NULL UNIQUE,
    status        VARCHAR(20)  NOT NULL CHECK (status IN ('PENDING', 'ACCEPTED', 'EXPIRED', 'CANCELLED')),
    role          VARCHAR(20)  NOT NULL CHECK (role IN ('USER', 'ADMIN')),
    created_at    TIMESTAMP    NOT NULL DEFAULT now(),
    expires_at    TIMESTAMP    NOT NULL
);

ALTER TABLE app_user
    ADD COLUMN family_id UUID REFERENCES family (id);
