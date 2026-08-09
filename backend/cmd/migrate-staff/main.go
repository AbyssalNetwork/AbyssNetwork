// Command migrate-staff imports the legacy config/staff.json into the
// abyss-api backend. Usage:
//
//	go run ./cmd/migrate-staff -file ../../config/staff.json -api http://localhost:8080
package main

import (
	"bytes"
	"encoding/json"
	"flag"
	"fmt"
	"io"
	"log"
	"net/http"
	"os"
	"strings"
	"time"

	"github.com/AbyssalNetwork/abyss-backend/internal/store"
)

// staffFile mirrors the old StaffManager file layout: a "staff" map keyed by
// UUID. Reuses store.StaffUpsert so serialization matches the API contract.
type staffFile struct {
	Staff map[string]store.StaffUpsert `json:"staff"`
}

func main() {
	file := flag.String("file", "config/staff.json", "path to the legacy staff.json")
	api := flag.String("api", "http://localhost:8080", "abyss-api base URL")
	flag.Parse()

	data, err := os.ReadFile(*file)
	if err != nil {
		log.Fatalf("read %s: %v", *file, err)
	}

	var wrapper staffFile
	if err := json.Unmarshal(data, &wrapper); err != nil {
		log.Fatalf("parse %s: %v", *file, err)
	}
	if len(wrapper.Staff) == 0 {
		log.Println("no staff entries found; nothing to migrate")
		return
	}

	client := &http.Client{Timeout: 10 * time.Second}
	base := strings.TrimRight(*api, "/")

	migrated, failed := 0, 0
	for mapKey, member := range wrapper.Staff {
		if member.UUID == "" {
			member.UUID = mapKey
		}

		body, err := json.Marshal(member)
		if err != nil {
			log.Printf("SKIP %s: marshal: %v", mapKey, err)
			failed++
			continue
		}

		resp, err := client.Post(base+"/staff", "application/json", bytes.NewReader(body))
		if err != nil {
			log.Printf("FAIL %s (%s): %v", name(member), mapKey, err)
			failed++
			continue
		}
		_, _ = io.Copy(io.Discard, resp.Body)
		resp.Body.Close()

		if resp.StatusCode != http.StatusOK {
			log.Printf("FAIL %s (%s): API returned %d", name(member), mapKey, resp.StatusCode)
			failed++
			continue
		}
		log.Printf("OK %s (%s) as %s", name(member), mapKey, rank(member))
		migrated++
	}

	fmt.Printf("done: %d migrated, %d failed\n", migrated, failed)
	if failed > 0 {
		os.Exit(1)
	}
}

func name(m store.StaffUpsert) string {
	if m.LastKnownName == nil || *m.LastKnownName == "" {
		return "<no name>"
	}
	return *m.LastKnownName
}

func rank(m store.StaffUpsert) string {
	if m.Rank == nil {
		return "<no rank>"
	}
	return *m.Rank
}
