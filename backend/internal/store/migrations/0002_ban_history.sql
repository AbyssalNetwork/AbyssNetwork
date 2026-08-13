CREATE TABLE IF NOT EXISTS ban_history (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    uuid        VARCHAR(36)  NOT NULL,
    username    VARCHAR(16)  NOT NULL,
    reason      VARCHAR(255) NOT NULL,
    banned_by   VARCHAR(36)  NOT NULL,
    banned_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    unbanned_by VARCHAR(36)  NULL,
    unbanned_at TIMESTAMP    NULL,
    INDEX idx_ban_history_uuid (uuid)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;
