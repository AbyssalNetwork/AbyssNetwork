package store

import (
	"context"
	"database/sql"
	"errors"
	"fmt"
	"strings"
)

type Player struct {
	UUID     string `json:"uuid"`
	Username string `json:"username"`
	Kills    int    `json:"kills"`
	Deaths   int    `json:"deaths"`
	Team     int    `json:"team"`
	Rank     string `json:"player_rank"`
	IsOpped  bool   `json:"is_opped"`
}

// PlayerUpsert is an upsert payload. Pointer fields are optional: on a
// conflicting row only the provided fields are updated, existing values are
// preserved. On a fresh insert, omitted fields fall back to column defaults.
type PlayerUpsert struct {
	UUID     string  `json:"uuid"`
	Username *string `json:"username"`
	Kills    *int    `json:"kills"`
	Deaths   *int    `json:"deaths"`
	Team     *int    `json:"team"`
	Rank     *string `json:"player_rank"`
	IsOpped  *bool   `json:"is_opped"`
}

type StaffMember struct {
	UUID          string `json:"uuid"`
	LastKnownName string `json:"lastKnownName"`
	Rank          string `json:"rank"`
	Vanished      bool   `json:"vanished"`
}

type StaffUpsert struct {
	UUID          string  `json:"uuid"`
	LastKnownName *string `json:"lastKnownName"`
	Rank          *string `json:"rank"`
	Vanished      *bool   `json:"vanished"`
}

// StatsUpdate carries kill/death deltas. Values are added to the stored stats,
// not set — the row is created on demand if the player has never joined.
type StatsUpdate struct {
	Username string `json:"username"`
	Kills    int    `json:"kills"`
	Deaths   int    `json:"deaths"`
}

func (s *Store) UpsertPlayer(ctx context.Context, p PlayerUpsert) error {
	if strings.TrimSpace(p.UUID) == "" {
		return fmt.Errorf("%w: uuid is required", ErrValidation)
	}
	if p.Username == nil {
		return fmt.Errorf("%w: username is required", ErrValidation)
	}

	cols := []string{"uuid"}
	vals := []any{p.UUID}
	var updates []string

	add := func(col string, v any) {
		cols = append(cols, col)
		vals = append(vals, v)
		updates = append(updates, col+" = VALUES("+col+")")
	}

	if p.Username != nil {
		add("username", *p.Username)
	}
	if p.Kills != nil {
		add("kills", *p.Kills)
	}
	if p.Deaths != nil {
		add("deaths", *p.Deaths)
	}
	if p.Team != nil {
		add("team", *p.Team)
	}
	if p.Rank != nil {
		add("player_rank", *p.Rank)
	}
	if p.IsOpped != nil {
		add("is_opped", *p.IsOpped)
	}

	if len(updates) == 0 {
		return fmt.Errorf("%w: no fields provided", ErrValidation)
	}

	query := "INSERT INTO players (" + strings.Join(cols, ", ") + ") VALUES (" +
		strings.TrimSuffix(strings.Repeat("?, ", len(cols)), ", ") +
		") ON DUPLICATE KEY UPDATE " + strings.Join(updates, ", ")

	_, err := s.db.ExecContext(ctx, query, vals...)
	return err
}

func (s *Store) GetPlayer(ctx context.Context, uuid string) (*Player, error) {
	var p Player
	err := s.db.QueryRowContext(ctx,
		"SELECT uuid, username, kills, deaths, team, player_rank, is_opped FROM players WHERE uuid = ?",
		uuid,
	).Scan(&p.UUID, &p.Username, &p.Kills, &p.Deaths, &p.Team, &p.Rank, &p.IsOpped)
	if errors.Is(err, sql.ErrNoRows) {
		return nil, ErrNotFound
	}
	if err != nil {
		return nil, err
	}
	return &p, nil
}

// GetPlayerByUsername looks a player up by username (case-insensitive).
func (s *Store) GetPlayerByUsername(ctx context.Context, username string) (*Player, error) {
	if strings.TrimSpace(username) == "" {
		return nil, fmt.Errorf("%w: username is required", ErrValidation)
	}
	var p Player
	err := s.db.QueryRowContext(ctx,
		"SELECT uuid, username, kills, deaths, team, player_rank, is_opped FROM players WHERE LOWER(username) = LOWER(?) LIMIT 1",
		username,
	).Scan(&p.UUID, &p.Username, &p.Kills, &p.Deaths, &p.Team, &p.Rank, &p.IsOpped)
	if errors.Is(err, sql.ErrNoRows) {
		return nil, ErrNotFound
	}
	if err != nil {
		return nil, err
	}
	return &p, nil
}

// ListPlayers returns players ordered by kills (descending), capped by limit.
func (s *Store) ListPlayers(ctx context.Context, limit int) ([]Player, error) {
	if limit <= 0 {
		limit = 100
	}
	rows, err := s.db.QueryContext(ctx,
		"SELECT uuid, username, kills, deaths, team, player_rank, is_opped FROM players ORDER BY kills DESC, username ASC LIMIT ?",
		limit,
	)
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	var out []Player
	for rows.Next() {
		var p Player
		if err := rows.Scan(&p.UUID, &p.Username, &p.Kills, &p.Deaths, &p.Team, &p.Rank, &p.IsOpped); err != nil {
			return nil, err
		}
		out = append(out, p)
	}
	return out, rows.Err()
}

func (s *Store) UpsertStaff(ctx context.Context, m StaffUpsert) error {
	if strings.TrimSpace(m.UUID) == "" {
		return fmt.Errorf("%w: uuid is required", ErrValidation)
	}
	if m.LastKnownName == nil || m.Rank == nil {
		return fmt.Errorf("%w: lastKnownName and rank are required", ErrValidation)
	}

	cols := []string{"uuid"}
	vals := []any{m.UUID}
	var updates []string

	add := func(col string, v any) {
		cols = append(cols, col)
		vals = append(vals, v)
		updates = append(updates, col+" = VALUES("+col+")")
	}

	add("last_known_name", *m.LastKnownName)
	add("rank", *m.Rank)
	if m.Vanished != nil {
		add("vanished", *m.Vanished)
	}

	query := "INSERT INTO staff (" + strings.Join(cols, ", ") + ") VALUES (" +
		strings.TrimSuffix(strings.Repeat("?, ", len(cols)), ", ") +
		") ON DUPLICATE KEY UPDATE " + strings.Join(updates, ", ")

	_, err := s.db.ExecContext(ctx, query, vals...)
	return err
}

func (s *Store) GetStaff(ctx context.Context, uuid string) (*StaffMember, error) {
	var m StaffMember
	err := s.db.QueryRowContext(ctx,
		"SELECT uuid, last_known_name, rank, vanished FROM staff WHERE uuid = ?",
		uuid,
	).Scan(&m.UUID, &m.LastKnownName, &m.Rank, &m.Vanished)
	if errors.Is(err, sql.ErrNoRows) {
		return nil, ErrNotFound
	}
	if err != nil {
		return nil, err
	}
	return &m, nil
}

func (s *Store) ListStaff(ctx context.Context) ([]StaffMember, error) {
	rows, err := s.db.QueryContext(ctx,
		"SELECT uuid, last_known_name, rank, vanished FROM staff ORDER BY last_known_name")
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	var out []StaffMember
	for rows.Next() {
		var m StaffMember
		if err := rows.Scan(&m.UUID, &m.LastKnownName, &m.Rank, &m.Vanished); err != nil {
			return nil, err
		}
		out = append(out, m)
	}
	return out, rows.Err()
}

func (s *Store) DeleteStaff(ctx context.Context, uuid string) error {
	res, err := s.db.ExecContext(ctx, "DELETE FROM staff WHERE uuid = ?", uuid)
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
	return nil
}

// AddStats applies kill/death deltas to a player. The row is created on demand
// (defaults) if the player has never joined, so stats survive even when the
// join-time upsert did not happen yet.
func (s *Store) AddStats(ctx context.Context, uuid string, u StatsUpdate) error {
	if strings.TrimSpace(uuid) == "" {
		return fmt.Errorf("%w: uuid is required", ErrValidation)
	}
	if strings.TrimSpace(u.Username) == "" {
		return fmt.Errorf("%w: username is required", ErrValidation)
	}
	_, err := s.db.ExecContext(ctx,
		`INSERT INTO players (uuid, username, kills, deaths) VALUES (?, ?, ?, ?)
		 ON DUPLICATE KEY UPDATE
		   username = VALUES(username),
		   kills = kills + VALUES(kills),
		   deaths = deaths + VALUES(deaths)`,
		uuid, u.Username, u.Kills, u.Deaths)
	return err
}
