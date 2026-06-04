ALTER TABLE app_user
    ADD COLUMN email_verified BOOLEAN NOT NULL DEFAULT FALSE;

UPDATE app_user SET email_verified = TRUE;

ALTER TABLE app_user ALTER COLUMN email_verified DROP DEFAULT;


CREATE TABLE email_verification_token
(
    id          UUID PRIMARY KEY     DEFAULT gen_random_uuid(),
    user_id     UUID        NOT NULL REFERENCES app_user (id) ON DELETE CASCADE,
    token_hash  CHAR(64)    NOT NULL UNIQUE,
    expires_at  TIMESTAMPTZ NOT NULL,
    consumed_at TIMESTAMPTZ,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);


