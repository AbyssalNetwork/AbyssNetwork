# Abyss Network — Tiltfile
# Run `tilt up` to launch the MariaDB database and the Go backend API.
# Tilt watches the backend sources, rebuilds the API on changes, and shows
# both services with live logs + status in the web UI (default: localhost:10350).

# Bring in both services (db + api) from the compose file
docker_compose("backend/docker-compose.yml")

# Rebuild the API image whenever files under backend/ change.
# This matches the api service's `image: abyss-api` ref from docker-compose.yml.
docker_build(
    "abyss-api",
    context="backend",
    dockerfile="backend/Dockerfile",
)
