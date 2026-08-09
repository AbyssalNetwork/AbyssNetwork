# Abyss Network

A custom Minecraft server built on [Minestom](https://minestom.net/), featuring weapon systems, world loading, an API-backed persistence layer, and environment-driven configuration.

---

## Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Requirements](#requirements)
- [Setup](#setup)
    - [Environment Variables](#environment-variables)
    - [World File](#world-file)
    - [Building](#building)
    - [Running](#running)
- [Project Structure](#project-structure)
- [Architecture](#architecture)
    - [Database](#database)
    - [Events](#events)
    - [Dev Mode](#dev-mode)
- [Dependencies](#dependencies)
- [Contributing](#contributing)

---

## Overview

Abyss Network is a Minestom-based game server for Minecraft 1.21.11. It replaces the standard Mojang server stack with a lightweight, custom implementation, giving full control over gameplay, world loading, player data, and server lifecycle.

The server supports both a **production mode** (API-backed persistence via the Go backend and online-mode authentication) and a **dev mode** (no persistence, offline-friendly, all players granted operator permissions automatically).

---

## Features

- **Online-mode authentication** via Minestom's `Auth.Online()`
- **API-backed persistence** — a Go backend service (`backend/`) owns a MariaDB/MySQL database and exposes a small REST API; the Java server persists players and staff through it asynchronously
- **Kill/death tracking** — weapon kills (via MineGun's custom health system) and vanilla deaths are tracked and synced to the database (`POST /players/{uuid}/stats`)
- **Stats viewing** — `/stats` (own) and `/stats <player>` (anyone, online or offline) in-game; web dashboard at `http://localhost:8080/stats` with auto-refresh
- **Polar world format** — worlds are loaded from `.polar` files and saved on shutdown
- **Custom weapon system** — Rifle and Rocket Launcher via the [MineGun](https://github.com/AbyssalNetwork/minegun) library
- **Block placement rules** — realistic block orientation and connection logic (stairs, slabs, fences, doors, signs, etc.)
- **Gamemode switching** — players with permission level ≥ 2 can change their own gamemode
- **Graceful shutdown** — world is saved to disk and the database connection is cleanly closed before the process exits
- **Custom logger** — colour-coded, timestamped console output with an ASCII banner on startup
- **Environment-based config** — all secrets and runtime flags live in a `.env` file, never in code

---

## Requirements

- **Java 25** (required by Minestom 2026.x)
- **Go 1.24+** (to build/run the backend)
- **MariaDB or MySQL** (production only — any recent version works; `backend/docker-compose.yml` spins one up)
- A Polar-format world file at `worlds/world.polar`
- Gradle (the wrapper `./gradlew` is included)

---

## Setup

### Environment Variables

The **server** reads a `.env` file in the project root. The following keys are recognised:

| Key | Required | Description |
|-----|----------|-------------|
| `TYPE` | Yes | Set to `dev` to enable dev mode (no API calls, permission level 4 for all players). Any other value enables production mode. |
| `ABYSS_API_URL` | Production | Base URL of the Go backend (e.g. `http://localhost:8080`) |

The **backend** reads `backend/.env` (see `backend/.env.example`):

| Key | Required | Description |
|-----|----------|-------------|
| `ABYSS_HTTP_ADDR` | No | HTTP listen address (default `:8080`) |
| `ABYSS_DB_HOST` | Yes | MariaDB/MySQL host (e.g. `localhost` or `db`) |
| `ABYSS_DB_PORT` | Yes | Database port (e.g. `3306`) |
| `ABYSS_DB_NAME` | Yes | Database name |
| `ABYSS_DB_USER` | Yes | Database username |
| `ABYSS_DB_PASSWORD` | Yes | Database password |

**Example `.env` for local development:**

```env
TYPE=dev
```

**Example `.env` for production:**

```env
TYPE=prod
ABYSS_API_URL=http://localhost:8080
```

> ⚠️ Never commit your `.env` file. Add it to `.gitignore`.

---

### World File

Place your Polar-format world at:

```
worlds/world.polar
```

The server will log an error and continue without a world if the file is missing. You can export a world to Polar format using the [Polar](https://github.com/hollow-cube/polar) tooling.

---

### Building

```bash
./gradlew build
```

The output JAR will be placed in `build/libs/`.

### Running (production)

Start the backend (MariaDB + API) with [Tilt](https://tilt.dev) — live status,
logs, and auto-rebuilds in a web UI:

```bash
tilt up
```

Or with plain Docker Compose:

```bash
cd backend
docker compose up -d      # or run MariaDB yourself and set backend/.env
go run .
```

Then run the server with `TYPE=prod` and `ABYSS_API_URL` set in `.env`:

```bash
./gradlew run
```

---

### Running

```bash
./gradlew run
```

Or run the built JAR directly:

```bash
java \
  --enable-native-access=ALL-UNNAMED \
  --add-modules=jdk.unsupported \
  -XX:+IgnoreUnrecognizedVMOptions \
  -jar build/libs/abyssnetwork-1.0-SNAPSHOT.jar
```

The server binds on `0.0.0.0:25565` by default.

---

## Project Structure

```
backend/                                # Go persistence API
│   main.go                             # Config, DB connection, HTTP server, graceful shutdown
│   Dockerfile / docker-compose.yml     # Containerised MariaDB + API
│   internal/store/                     # database/sql access + embedded schema
│   internal/httpapi/                   # REST handlers (players, staff, health)
│   cmd/migrate-staff/                  # One-shot importer for the legacy config/staff.json
│
Tiltfile                                # `tilt up` dev workflow (db + api, auto-rebuild)
│
src/main/java/org/vardinsdev/abyssnetwork/
│
├── Main.java                          # Server entry point, startup sequence
├── AbyssLogger.java                   # Colour-coded console logger
│
├── Database/
│   ├── Config.java                    # dotenv-driven config (TYPE, ABYSS_API_URL)
│   ├── ApiClient.java                 # Async HTTP client for the Go backend
│   └── PlayerSync.java                # Player row upsert on join
│
├── staff/
│   ├── StaffManager.java              # In-memory staff cache, write-through to API
│   ├── StaffMember.java / StaffRank.java
│
└── events/
    ├── PlayerConfigurationEvent.java  # Spawn point, dev-mode permissions
    └── GamemodeSwitcherEvent.java     # Permission-gated gamemode switching
```

---

## Architecture

### Database

Persistence is owned by the Go backend (`backend/`), not by the game server.

- The **Go service** connects to MariaDB/MySQL with a connection pool, creates the schema on startup, and exposes a small REST API (`POST/GET /players`, `GET /players/by-username/{username}`, `POST /players/{uuid}/stats`, `GET/POST/DELETE /staff`, `GET /health`). It also serves a stats dashboard at `GET /stats`.
- The **Java server** never talks to the database directly. `ApiClient` (`Database/ApiClient.java`) sends async HTTP requests via `HttpClient.sendAsync`, so database I/O never blocks the tick thread.
- `StaffManager` keeps a fast in-memory cache for reads on join and write-throughs every mutation to the API (`addStaff`, `updateStaff`, `removeStaff`).
- `PlayerSync` upserts the player's UUID/username on join; other stats are preserved by the backend on conflict.
- `KillTracker` (`events/KillTracker.java`) watches MineGun's custom health tag and `PlayerDeathEvent` to credit kills and deaths, pushing deltas to `POST /players/{uuid}/stats` asynchronously. It must be registered before `HealthManagement.register()` so it observes the health drop before MineGun resets it.

**Write authentication:** all mutating endpoints (`POST /players`, `POST /players/{uuid}/stats`, `POST /staff`, `DELETE /staff`) require `Authorization: Bearer <token>` where the token is `ABYSS_API_TOKEN`. The Java server reads the same variable from `.env` and sends it automatically. `GET` endpoints (stats dashboard, player lookups, staff listing) stay public, so the dashboard can be shared without exposing write access. If `ABYSS_API_TOKEN` is unset the API logs a warning and accepts writes without a token (local dev convenience).

**Schema** (created automatically by the backend on startup):

```sql
CREATE TABLE IF NOT EXISTS players (
    uuid        VARCHAR(36) PRIMARY KEY,
    username    VARCHAR(16) NOT NULL,
    kills       INT         DEFAULT 0,
    deaths      INT         DEFAULT 0,
    team        INT         DEFAULT -1,
    player_rank VARCHAR(32) DEFAULT 'default',
    is_opped    BOOLEAN     DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS staff (
    uuid            VARCHAR(36) PRIMARY KEY,
    last_known_name VARCHAR(16) NOT NULL,
    rank            VARCHAR(32) NOT NULL,
    vanished        BOOLEAN     DEFAULT FALSE
);
```

Player upserts use `INSERT ... ON DUPLICATE KEY UPDATE` so the username stays current while all other stats are preserved.

---

### Events

| Event class | Trigger | Behaviour |
|---|---|---|
| `PlayerConfigurationEvent` | `AsyncPlayerConfigurationEvent` | Sets spawn point; grants permission level 4 in dev mode |
| `GamemodeSwitcherEvent` | `PlayerGameModeRequestEvent` | Allows gamemode change if permission level ≥ 2 |
| `PlayerSync` | `AsyncPlayerConfigurationEvent` | Upserts the player row via the API (production only) |

---

### Dev Mode

When `TYPE=dev` is set in `.env`:

- No API calls are made — `ApiClient` is disabled and staff/player data is not persisted
- Every player who joins is automatically granted **permission level 4** (full operator)
- The server starts faster and works without any external infrastructure

Switch to any other value (e.g. `prod`) to enable online-mode auth and persistence via the Go backend.

---

## Dependencies

| Library | Purpose |
|---|---|
| [Minestom](https://github.com/minestom/Minestom) `2026.03.03-1.21.11` | Core server framework |
| [MineGun](https://github.com/AbyssalNetwork/minegun) `1.0.3` | Custom weapon system (Rifle, Rocket Launcher) |
| [Placement](https://github.com/minestom-extras/placement) `0.1.0` | Block placement rules |
| [Polar](https://github.com/hollow-cube/polar) `1.15.1` | Polar world format loader |
| [dotenv-java](https://github.com/cdimascio/dotenv-java) `3.2.0` | `.env` file parsing |
| [MariaDB JDBC](https://mariadb.com/kb/en/about-mariadb-connector-j/) `3.3.3` | Database driver |
| [MySQL Connector/J](https://dev.mysql.com/downloads/connector/j/) `9.3.0` | MySQL compatibility |
| [fastutil](https://fastutil.di.unimi.it/) `8.5.12` | High-performance collections |
| [SLF4J Simple](https://www.slf4j.org/) `2.0.13` | Logging backend |

---

## Contributing

1. Fork the repository and create a feature branch
2. Use `TYPE=dev` in your `.env` for local work — no database needed
3. Follow the existing package structure (`events/`, `Database/`, etc.)
4. Use `AbyssLogger` for all console output rather than `System.out.println` directly
5. Open a pull request with a clear description of what changed and why