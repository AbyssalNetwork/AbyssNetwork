package store

import (
	"strings"
	"testing"
)

func TestLoadMigrationsOrder(t *testing.T) {
	migrations, err := loadMigrations()
	if err != nil {
		t.Fatal(err)
	}
	if len(migrations) != 2 {
		t.Fatalf("expected 2 migrations, got %d", len(migrations))
	}
	versions := []string{migrations[0].Version, migrations[1].Version}
	if versions[0] != "0001_init" || versions[1] != "0002_ban_history" {
		t.Fatalf("unexpected migration order: %v", versions)
	}
	for _, m := range migrations {
		for _, stmt := range splitStatements(m.SQL) {
			if !strings.HasPrefix(stmt, "CREATE TABLE IF NOT EXISTS") {
				t.Errorf("%s: unexpected statement: %.60s", m.Version, stmt)
			}
		}
	}
}
