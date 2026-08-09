CREATE TABLE IF NOT EXISTS players (
    uuid        VARCHAR(36)  NOT NULL,
    username    VARCHAR(16)  NOT NULL,
    kills       INT          NOT NULL DEFAULT 0,
    deaths      INT          NOT NULL DEFAULT 0,
    team        INT          NOT NULL DEFAULT -1,
    player_rank VARCHAR(32)  NOT NULL DEFAULT 'default',
    is_opped    TINYINT(1)   NOT NULL DEFAULT 0,
    updated_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (uuid)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS staff (
    uuid            VARCHAR(36)  NOT NULL,
    last_known_name VARCHAR(16)  NOT NULL,
    rank            VARCHAR(32)  NOT NULL,
    vanished        TINYINT(1)   NOT NULL DEFAULT 0,
    updated_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (uuid)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;
