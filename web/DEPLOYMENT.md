# Deployment & Infrastructure Guide for pingspike.mikelcodez.xyz

## Current Dedicated Server Inventory & Safety Boundaries

The dedicated server is shared with existing production services:
- **Pelican Panel** (Game server orchestration)
- **Nextcloud** (Personal / family cloud storage)
- **Ollama + OpenWebUI** (Local AI inference service)
- **PDF Pro Panel** (Father's active business tool)
- **Lighttpd / Web Server & Reverse Proxy**

> [!CAUTION]
> **Zero-Risk Principle**: Deployment must never overwrite, restart, or alter existing reverse proxy configurations (such as pdfpropanel, Nextcloud, or Pelican routes) or touch system files outside the isolated deployment directory.

---

## 1. Website Structure (`web/`)

The website in `C:\MinecraftDev\PingSpikeIndicator\web` is completely self-contained with static assets and standalone `.jar` downloads:

```
web/
├── public/
│   ├── index.html         # High-converting landing page with OpenGraph tags
│   ├── style.css          # Dark-mode gaming UI (Ember/Cyan/Toxic/Void styles)
│   ├── script.js         # Interactive HUD theme switcher & spike simulator
│   ├── icon.png           # Mod branding icon
│   └── downloads/         # Pre-populated with release artifacts
│       ├── pingspikeindicator-1.0.0-1.20.1.jar
│       ├── pingspikeindicator-1.0.0-1.21-1.21.10.jar
│       ├── pingspikeindicator-1.0.0-1.21.11.jar
│       └── pingspikeindicator-1.0.0-26.x.jar
├── docker-compose.yml     # Optional zero-footprint isolated container runner
└── nginx-site.conf        # Isolated Nginx / Lighttpd vhost snippet
```

---

## 2. Safe Integration Architectures

### Option A (Recommended): Isolated Docker Container + Internal Port
Run the static site in a lightweight, read-only alpine container mapping only to an internal port (e.g. `127.0.0.1:8088`):
```yaml
# docker-compose.yml
services:
  pingspike-web:
    image: nginx:alpine
    container_name: pingspike-website
    restart: unless-stopped
    ports:
      - "127.0.0.1:8088:80"
    volumes:
      - ./public:/usr/share/nginx/html:ro
```
Your existing reverse proxy (Lighttpd or Nginx) only needs **one single proxy rule** routing `pingspike.mikelcodez.xyz` to `http://127.0.0.1:8088`. Zero risk to existing sites.

### Option B: Standalone Lighttpd VHost Directory
If serving purely via existing Lighttpd:
```lighttpd
$HTTP["host"] == "pingspike.mikelcodez.xyz" {
    server.document-root = "/var/www/pingspike/public"
    index-file.names = ( "index.html" )
}
```

---

## 3. Remote Deployment Strategies

When you are ready to connect to your dedicated server, we can use one of the following approaches:

### Strategy 1: Dedicated Deploy User via SSH Key (Least Privilege)
1. Create a non-root user on the server (e.g. `pingspike-deploy`):
   ```bash
   sudo useradd -m -s /bin/bash pingspike-deploy
   ```
2. Grant write access **only** to `/var/www/pingspike` or `~/pingspike-web`.
3. Provide the SSH host, port, user, and public key / private key path.
4. An automated deployment script can rsync / scp the contents of `web/public` directly.

### Strategy 2: Git Webhook / Server Git Remote
1. Initialize a bare git repository on the server with a `post-receive` hook.
2. Pushing from the local machine instantly unpacks the static site and release `.jar`s into the webroot without requiring root access.

### Strategy 3: Read-Only Discovery Exploration First
When you provide SSH credentials or run a diagnostics script:
1. We run non-destructive inspection commands (`ss -tulpn`, `ps aux`, web server configs in read-only mode).
2. We map out the exact ports in use (Pelican, Nextcloud, Ollama, Lighttpd) to ensure we pick an unused port.
3. We draft the exact vhost configuration for your review before touching any files.
