package store

import (
	"context"
	"database/sql"
	"errors"
	"fmt"
	"strings"
	"time"
)

type Ban struct {
	UUID     string    `json:"uuid"`
	Username string    `json:"username"`
	Reason   string    `json:"reason"`
	BannedBy string    `json:"bannedBy"`
	BannedAt time.Time `json:"bannedAt"`
}

type BanHistory struct {
	ID         int64      `json:"id"`
	UUID       string     `json:"uuid"`
	Username   string     `json:"username"`
	Reason     string     `json:"reason"`
	BannedBy   string     `json:"bannedBy"`
	BannedAt   time.Time  `json:"bannedAt"`
	UnbannedBy *string    `json:"unbannedBy,omitempty"`
	UnbannedAt *time.Time `json:"unbannedAt,omitempty"`
}

func (s *Store) BanPlayer(ctx context.Context, b Ban) error {
	if strings.TrimSpace(b.UUID) == "" {
		return fmt.Errorf("%w: uuid is required", ErrValidation)
	}
	if strings.TrimSpace(b.Username) == "" {
		return fmt.Errorf("%w: username is required", ErrValidation)
	}
	if strings.TrimSpace(b.Reason) == "" {
		return fmt.Errorf("%w: reason is required", ErrValidation)
	}
	_, err := s.db.ExecContext(ctx,
		`INSERT INTO bans (uuid, username, reason, banned_by) VALUES (?, ?, ?, ?)
		 ON DUPLICATE KEY UPDATE
		   username = VALUES(username),
		   reason = VALUES(reason),
		   banned_by = VALUES(banned_by)`,
		b.UUID, b.Username, b.Reason, b.BannedBy)
	if err != nil {
		return err
	}
	_, err = s.db.ExecContext(ctx,
		`INSERT INTO ban_history (uuid, username, reason, banned_by) VALUES (?, ?, ?, ?)`,
		b.UUID, b.Username, b.Reason, b.BannedBy)
	return err
}

func (s *Store) GetBan(ctx context.Context, uuid string) (*Ban, error) {
	if strings.TrimSpace(uuid) == "" {
		return nil, fmt.Errorf("%w: uuid is required", ErrValidation)
	}
	var b Ban
	err := s.db.QueryRowContext(ctx,
		"SELECT uuid, username, reason, banned_by, banned_at FROM bans WHERE uuid = ?",
		uuid,
	).Scan(&b.UUID, &b.Username, &b.Reason, &b.BannedBy, &b.BannedAt)
	if errors.Is(err, sql.ErrNoRows) {
		return nil, ErrNotFound
	}
	if err != nil {
		return nil, err
	}
	return &b, nil
}

func (s *Store) Unban(ctx context.Context, uuid string, unbannedBy string) error {
	if strings.TrimSpace(uuid) == "" {
		return fmt.Errorf("%w: uuid is required", ErrValidation)
	}
	res, err := s.db.ExecContext(ctx, "DELETE FROM bans WHERE uuid = ?", uuid)
	if err != nil {
		return err
	}
	n, err := res.RowsAffected()
	if err != nil {
		return err
	}
	if n == 0 {
		return ErrNotFound
	}
	_, err = s.db.ExecContext(ctx,
		"UPDATE ban_history SET unbanned_by = ?, unbanned_at = NOW() WHERE uuid = ? AND unbanned_at IS NULL",
		unbannedBy, uuid)
	return err
}

func (s *Store) ListBanHistory(ctx context.Context) ([]BanHistory, error) {
	rows, err := s.db.QueryContext(ctx,
		`SELECT id, uuid, username, reason, banned_by, banned_at, unbanned_by, unbanned_at
		 FROM ban_history ORDER BY banned_at DESC`)
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	history := make([]BanHistory, 0)
	for rows.Next() {
		var h BanHistory
		var unbannedBy sql.NullString
		var unbannedAt sql.NullTime
		if err := rows.Scan(&h.ID, &h.UUID, &h.Username, &h.Reason, &h.BannedBy, &h.BannedAt,
			&unbannedBy, &unbannedAt); err != nil {
			return nil, err
		}
		if unbannedBy.Valid {
			h.UnbannedBy = &unbannedBy.String
		}
		if unbannedAt.Valid {
			h.UnbannedAt = &unbannedAt.Time
		}
		history = append(history, h)
	}
	return history, rows.Err()
}
