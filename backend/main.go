package main

import (
	"context"
	"errors"
	"fmt"
	"log"
	"net/http"
	"os"
	"os/signal"
	"syscall"
	"time"

	"github.com/joho/godotenv"

	"github.com/AbyssalNetwork/abyss-backend/internal/httpapi"
	"github.com/AbyssalNetwork/abyss-backend/internal/store"
)

func envOr(key, def string) string {
	if v := os.Getenv(key); v != "" {
		return v
	}
	return def
}

func main() {
	// Optional: load backend/.env when present. Real deployments set env vars directly.
	_ = godotenv.Load()

	host := envOr("ABYSS_DB_HOST", "localhost")
	port := envOr("ABYSS_DB_PORT", "3306")
	name := envOr("ABYSS_DB_NAME", "abyssnetwork")
	user := envOr("ABYSS_DB_USER", "abyss")
	pass := os.Getenv("ABYSS_DB_PASSWORD")
	addr := envOr("ABYSS_HTTP_ADDR", ":8080")
	token := os.Getenv("ABYSS_API_TOKEN")

	dsn := fmt.Sprintf(
		"%s:%s@tcp(%s:%s)/%s?parseTime=true&charset=utf8mb4&collation=utf8mb4_unicode_ci",
		user, pass, host, port, name,
	)

	logger := log.New(os.Stdout, "[abyss-api] ", log.LstdFlags)

	// Retry until the database is reachable. The container may start before
	// MariaDB is healthy under `docker compose up` / `tilt up`.
	const (
		maxAttempts = 30
		retryDelay  = 2 * time.Second
	)
	var st *store.Store
	var err error
	for attempt := 1; attempt <= maxAttempts; attempt++ {
		if st, err = store.Open(dsn); err == nil {
			break
		}
		logger.Printf("database not ready (attempt %d/%d): %v", attempt, maxAttempts, err)
		time.Sleep(retryDelay)
	}
	if st == nil {
		logger.Fatalf("database: %v", err)
	}
	defer st.Close()

	ctx, cancel := context.WithTimeout(context.Background(), 10*time.Second)
	defer cancel()
	if err := st.EnsureSchema(ctx); err != nil {
		logger.Fatalf("schema: %v", err)
	}

	logger.Printf("connected to %s at %s:%s/%s", user, host, port, name)

	if token == "" {
		logger.Printf("WARNING: no ABYSS_API_TOKEN set - write endpoints are unauthenticated (local dev only)")
	}

	srv := &http.Server{
		Addr:         addr,
		Handler:      httpapi.New(st, token).Handler(),
		ReadTimeout:  10 * time.Second,
		WriteTimeout: 10 * time.Second,
		IdleTimeout:  60 * time.Second,
	}

	go func() {
		logger.Printf("listening on %s", addr)
		if err := srv.ListenAndServe(); err != nil && !errors.Is(err, http.ErrServerClosed) {
			logger.Fatalf("http: %v", err)
		}
	}()

	stop := make(chan os.Signal, 1)
	signal.Notify(stop, os.Interrupt, syscall.SIGTERM)
	<-stop

	ctx, cancel = context.WithTimeout(context.Background(), 10*time.Second)
	defer cancel()
	if err := srv.Shutdown(ctx); err != nil {
		logger.Printf("shutdown: %v", err)
	}
	logger.Printf("shutdown complete")
}
