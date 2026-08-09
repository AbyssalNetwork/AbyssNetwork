package store

import "embed"

//go:embed schema.sql
var schemaFS embed.FS

var schemaSQL = mustSchema()

func mustSchema() string {
	b, err := schemaFS.ReadFile("schema.sql")
	if err != nil {
		panic("embedded schema.sql: " + err.Error())
	}
	return string(b)
}
