package store

import (
	"strings"
	"testing"
)

func TestSchemaStatements(t *testing.T) {
	stmts := schemaStatements()
	if len(stmts) != 3 {
		t.Fatalf("expected 3 schema statements, got %d: %q", len(stmts), stmts)
	}
	for _, s := range stmts {
		if !strings.HasPrefix(s, "CREATE TABLE IF NOT EXISTS") {
			t.Errorf("unexpected statement: %.60s", s)
		}
	}
}
