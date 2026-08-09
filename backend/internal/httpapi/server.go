package httpapi

import (
	"encoding/json"
	"errors"
	"io"
	"log"
	"net/http"
	"time"

	"github.com/AbyssalNetwork/abyss-backend/internal/store"
)

type Server struct {
	store *store.Store
	log   *log.Logger
	// writeToken, when non-empty, must be sent as "Authorization: Bearer <token>"
	// on every mutating (POST/DELETE) request. GET endpoints stay public.
	writeToken string
}

func New(st *store.Store, writeToken string) *Server {
	return &Server{store: st, log: log.Default(), writeToken: writeToken}
}

func (s *Server) Handler() http.Handler {
	mux := http.NewServeMux()
	mux.HandleFunc("GET /health", s.handleHealth)
	mux.HandleFunc("GET /{$}", s.handleStatsPage)
	mux.HandleFunc("GET /stats", s.handleStatsPage)
	mux.HandleFunc("POST /players", s.requireWriteAuth(s.handleUpsertPlayer))
	mux.HandleFunc("GET /players", s.handleListPlayers)
	mux.HandleFunc("GET /players/by-username/{username}", s.handleGetPlayerByUsername)
	mux.HandleFunc("GET /players/{uuid}", s.handleGetPlayer)
	mux.HandleFunc("POST /players/{uuid}/stats", s.requireWriteAuth(s.handleAddStats))
	mux.HandleFunc("GET /staff", s.handleListStaff)
	mux.HandleFunc("POST /staff", s.requireWriteAuth(s.handleUpsertStaff))
	mux.HandleFunc("GET /staff/{uuid}", s.handleGetStaff)
	mux.HandleFunc("DELETE /staff/{uuid}", s.requireWriteAuth(s.handleDeleteStaff))
	return s.recoverPanic(s.logRequests(mux))
}

// requireWriteAuth gates mutating endpoints. When no token is configured
// (local dev) writes are allowed without a token.
func (s *Server) requireWriteAuth(next http.HandlerFunc) http.HandlerFunc {
	return func(w http.ResponseWriter, r *http.Request) {
		if s.writeToken != "" {
			if r.Header.Get("Authorization") != "Bearer "+s.writeToken {
				writeError(w, http.StatusUnauthorized, "missing or invalid API token")
				return
			}
		}
		next(w, r)
	}
}

func (s *Server) handleHealth(w http.ResponseWriter, _ *http.Request) {
	writeJSON(w, http.StatusOK, map[string]string{"status": "ok"})
}

func decodeJSON(w http.ResponseWriter, r *http.Request, v any) error {
	dec := json.NewDecoder(io.LimitReader(r.Body, 1<<20))
	dec.DisallowUnknownFields()
	if err := dec.Decode(v); err != nil {
		writeError(w, http.StatusBadRequest, "invalid JSON body: "+err.Error())
		return err
	}
	return nil
}

func writeJSON(w http.ResponseWriter, status int, v any) {
	w.Header().Set("Content-Type", "application/json")
	w.WriteHeader(status)
	if v != nil {
		_ = json.NewEncoder(w).Encode(v)
	}
}

func writeError(w http.ResponseWriter, status int, msg string) {
	writeJSON(w, status, map[string]string{"error": msg})
}

func (s *Server) writeStoreError(w http.ResponseWriter, err error) {
	switch {
	case errors.Is(err, store.ErrNotFound):
		writeError(w, http.StatusNotFound, "record not found")
	case errors.Is(err, store.ErrValidation):
		writeError(w, http.StatusBadRequest, err.Error())
	default:
		s.log.Printf("internal error: %v", err)
		writeError(w, http.StatusInternalServerError, "internal server error")
	}
}

func (s *Server) logRequests(next http.Handler) http.Handler {
	return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		start := time.Now()
		rec := &statusRecorder{ResponseWriter: w, status: http.StatusOK}
		next.ServeHTTP(rec, r)
		s.log.Printf("%s %s -> %d (%s)", r.Method, r.URL.Path, rec.status, time.Since(start).Round(time.Microsecond))
	})
}

func (s *Server) recoverPanic(next http.Handler) http.Handler {
	return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		defer func() {
			if rec := recover(); rec != nil {
				s.log.Printf("panic: %v", rec)
				writeError(w, http.StatusInternalServerError, "internal server error")
			}
		}()
		next.ServeHTTP(w, r)
	})
}

type statusRecorder struct {
	http.ResponseWriter
	status int
}

func (r *statusRecorder) WriteHeader(code int) {
	r.status = code
	r.ResponseWriter.WriteHeader(code)
}
