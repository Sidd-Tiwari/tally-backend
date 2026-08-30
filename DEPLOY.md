# Deploying to Render

This deploys the whole backend as: **one public Gateway** + **three private services** + **one Postgres instance holding 3 logical databases**. Only the Gateway is reachable from the internet.

## Why the architecture changed from local dev

| Local dev | Render |
|---|---|
| Eureka for service discovery | Removed — Render gives every service a stable address, so the gateway routes to static URLs from env vars instead |
| 3 Postgres containers | 1 free Postgres instance, holding 3 databases (`auth_db`, `expense_db`, `habit_db`) — free tier is tight, this keeps real separation without 3x the cost |
| All services reachable directly | Only `api-gateway` is public. `auth-service`, `expense-service`, `habit-service` are Render **private services** — unreachable from outside your Render workspace, so the gateway's auth/CORS/rate-limiting can't be bypassed |

## One-time setup

**1. Push this repo to GitHub** (Render deploys from a Git repo).

**2. In the Render Dashboard**: New → Blueprint → connect your repo. Render reads `render.yaml` and shows you everything it's about to create.

**3. You'll be prompted for the `JWT_SECRET` values** (marked `sync: false` in `render.yaml` so it's never committed to Git). Generate one strong secret and **use the same value for all 4 services** — they all sign/verify with it:

```bash
openssl rand -base64 32
```

**4. Click "Deploy Blueprint".** Render provisions the Postgres instance and all 4 services. First deploy takes a few minutes (Maven build per service).

**5. Create the extra databases** — Render only creates the one database named in `render.yaml` (`auth_db`). Run `render-deploy-setup.sql` once against the instance (instructions inside the file) to add `expense_db` and `habit_db`.

**6. Find your public URL** — it's the `api-gateway` service's URL, something like `https://api-gateway-xxxx.onrender.com`. That's the one link you give out.

## What "public people" can do with it

Two separate things, both already built:

- **End users** register/login via `/api/auth/register` and `/api/auth/login` and use short-lived tokens (24h) — this is the normal app-user flow, same as the frontend uses.
- **Developers building on your API** call `POST /api/auth/api-keys` (while logged in) to generate a long-lived token (1 year) they can hardcode into their own app instead of re-logging-in constantly. See `API_REFERENCE.md` for the full endpoint list and an example integration.

Both are the exact same JWT mechanism under the hood — a "developer API key" is just a token with a longer expiry, so no separate auth system was needed.

## Free-tier tradeoffs to know about

- **Cold starts.** Free services sleep after 15 min idle; the next request takes 30-60s. This affects your Gateway (public-facing) and the 3 private services behind it, so a truly cold request could stack multiple wake-ups. If this matters for real users, upgrading just the Gateway to a $7/mo Starter plan (keeping the private services free) is the highest-leverage fix.
- **API keys aren't truly revocable yet.** Because they're stateless JWTs, deleting a key's record in `/api/auth/api-keys` stops it showing in the user's list but doesn't invalidate the token itself before it expires (up to a year). Rotating `JWT_SECRET` invalidates *everything* at once (all users get logged out) if you ever need a hard reset. A proper fix is a shared revocation check (Redis or a DB lookup) on every request — worth adding before this handles anything sensitive.
- **Rate limiting is in-memory**, not shared across instances. Fine at 1 instance (which free tier is anyway); if you ever scale the Gateway to multiple instances, this stops being accurate per-client and needs a Redis-backed limiter instead.
- **Free Postgres expires after 30 days** unless upgraded to a paid plan (from ~$6/mo). Set a calendar reminder, or upgrade before that.
- **CORS is wide open (`*`)** by default, since you don't know who's building against this yet. Once you have known frontend domains, set `ALLOWED_ORIGINS` (comma-separated) in the Gateway's environment variables to lock it down.

## Updating after the first deploy

Push to your connected branch — Render auto-redeploys. Blueprint changes (new env vars, new services) need a re-sync: Dashboard → your Blueprint → "Sync".
