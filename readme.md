# Abyss Network

A custom Minecraft server built on [Minestom](https://minestom.net/), featuring weapon systems, world loading, database-backed player management, and environment-driven configuration.

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

The server supports both a **production mode** (with MariaDB player persistence and online-mode authentication) and a **dev mode** (no database, offline-friendly, all players granted operator permissions automatically).

---

## Features

- **Online-mode authentication** via Minestom's `Auth.Online()`
- **MariaDB player persistence** — player UUIDs, usernames, kills, deaths, team, rank, and op status are stored and synced on join
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
- **MariaDB** (production only — any recent version works)
- A Polar-format world file at `worlds/world.polar`
- Gradle (the wrapper `./gradlew` is included)

---

## Setup

### Environment Variables

Create a `.env` file in the project root. The following keys are recognised:

| Key | Required | Description |
|-----|----------|-------------|
| `TYPE` | Yes | Set to `dev` to enable dev mode (no DB, permission level 4 for all players). Any other value enables production mode. |
| `DB_HOST` | Production | MariaDB host (e.g. `localhost`) |
| `DB_PORT` | Production | MariaDB port (e.g. `3306`) |
| `DB_NAME` | Production | Database name |
| `DB_USER` | Production | Database username |
| `DB_PASSWORD` | Production | Database password |

**Example `.env` for local development:**

```env
TYPE=dev
```

**Example `.env` for production:**

```env
TYPE=prod
DB_HOST=localhost
DB_PORT=3306
DB_NAME=abyssnetwork
DB_USER=abyss
DB_PASSWORD=supersecretpassword
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
src/main/java/org/vardinsdev/abyssnetwork/
│
├── Main.java                          # Server entry point, startup sequence
├── AbyssLogger.java                   # Colour-coded console logger
├── envHandler.java                    # Dotenv loader wrapper
│
├── Database/
│   └── DatabaseManager.java           # MariaDB connection & table creation
│
└── events/
    ├── PlayerConfigurationEvent.java  # Spawn point, dev-mode permissions
    └── GamemodeSwitcherEvent.java     # Permission-gated gamemode switching
```

---

## Architecture

### Database

`DatabaseManager` holds a single static `Connection` to MariaDB. On startup (production mode only), it:

1. Connects using credentials from `.env`
2. Creates the `players` table if it does not already exist

**Schema:**

```sql
CREATE TABLE IF NOT EXISTS players (
    uuid         VARCHAR(36)  PRIMARY KEY,
    username     VARCHAR(16)  NOT NULL,
    kills        INT          DEFAULT 0,
    deaths       INT          DEFAULT 0,
    team         INT          DEFAULT -1,
    player_rank  VARCHAR(32)  DEFAULT 'default',
    is_opped     BOOLEAN      DEFAULT FALSE
);
```

On each player join, an `INSERT ... ON DUPLICATE KEY UPDATE` query upserts the player's row, keeping the username current while preserving all other stats.

---

### Events

| Event class | Trigger | Behaviour |
|---|---|---|
| `PlayerConfigurationEvent` | `AsyncPlayerConfigurationEvent` | Sets spawn point; grants permission level 4 in dev mode |
| `GamemodeSwitcherEvent` | `PlayerGameModeRequestEvent` | Allows gamemode change if permission level ≥ 2 |
| `registerEvents()` (in `Main`) | `AsyncPlayerConfigurationEvent` | Upserts the player row in the database (production only) |

---

### Dev Mode

When `TYPE=dev` is set in `.env`:

- No database connection is attempted
- Every player who joins is automatically granted **permission level 4** (full operator)
- The server starts faster and works without any external infrastructure

Switch to any other value (e.g. `prod`) to enable online-mode auth and database sync.

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