package httpapi

import (
	"net/http"
	"strconv"

	"github.com/AbyssalNetwork/abyss-backend/internal/store"
)

func (s *Server) handleUpsertPlayer(w http.ResponseWriter, r *http.Request) {
	var p store.PlayerUpsert
	if err := decodeJSON(w, r, &p); err != nil {
		return
	}
	if err := s.store.UpsertPlayer(r.Context(), p); err != nil {
		s.writeStoreError(w, err)
		return
	}
	writeJSON(w, http.StatusOK, map[string]string{"status": "ok"})
}

func (s *Server) handleGetPlayer(w http.ResponseWriter, r *http.Request) {
	p, err := s.store.GetPlayer(r.Context(), r.PathValue("uuid"))
	if err != nil {
		s.writeStoreError(w, err)
		return
	}
	writeJSON(w, http.StatusOK, p)
}

func (s *Server) handleGetPlayerByUsername(w http.ResponseWriter, r *http.Request) {
	p, err := s.store.GetPlayerByUsername(r.Context(), r.PathValue("username"))
	if err != nil {
		s.writeStoreError(w, err)
		return
	}
	writeJSON(w, http.StatusOK, p)
}

func (s *Server) handleListPlayers(w http.ResponseWriter, r *http.Request) {
	limit := 100
	if raw := r.URL.Query().Get("limit"); raw != "" {
		if n, err := strconv.Atoi(raw); err == nil && n > 0 {
			limit = n
		}
	}
	players, err := s.store.ListPlayers(r.Context(), limit)
	if err != nil {
		s.writeStoreError(w, err)
		return
	}
	if players == nil {
		players = []store.Player{}
	}
	writeJSON(w, http.StatusOK, players)
}

func (s *Server) handleStatsPage(w http.ResponseWriter, r *http.Request) {
	w.Header().Set("Content-Type", "text/html; charset=utf-8")
	_, _ = w.Write([]byte(dashboardHTML))
}

func (s *Server) handleAddStats(w http.ResponseWriter, r *http.Request) {
	var u store.StatsUpdate
	if err := decodeJSON(w, r, &u); err != nil {
		return
	}
	if err := s.store.AddStats(r.Context(), r.PathValue("uuid"), u); err != nil {
		s.writeStoreError(w, err)
		return
	}
	writeJSON(w, http.StatusOK, map[string]string{"status": "ok"})
}

func (s *Server) handleListStaff(w http.ResponseWriter, r *http.Request) {
	staff, err := s.store.ListStaff(r.Context())
	if err != nil {
		s.writeStoreError(w, err)
		return
	}
	if staff == nil {
		staff = []store.StaffMember{}
	}
	writeJSON(w, http.StatusOK, staff)
}

func (s *Server) handleUpsertStaff(w http.ResponseWriter, r *http.Request) {
	var m store.StaffUpsert
	if err := decodeJSON(w, r, &m); err != nil {
		return
	}
	if err := s.store.UpsertStaff(r.Context(), m); err != nil {
		s.writeStoreError(w, err)
		return
	}
	writeJSON(w, http.StatusOK, map[string]string{"status": "ok"})
}

func (s *Server) handleGetStaff(w http.ResponseWriter, r *http.Request) {
	m, err := s.store.GetStaff(r.Context(), r.PathValue("uuid"))
	if err != nil {
		s.writeStoreError(w, err)
		return
	}
	writeJSON(w, http.StatusOK, m)
}

func (s *Server) handleDeleteStaff(w http.ResponseWriter, r *http.Request) {
	if err := s.store.DeleteStaff(r.Context(), r.PathValue("uuid")); err != nil {
		s.writeStoreError(w, err)
		return
	}
	w.WriteHeader(http.StatusNoContent)
}
