# Personal Tracker API — Reference

Base URL: `https://<your-gateway-domain>` (everything below is relative to this — e.g. `/api/auth/login` means `https://<your-gateway-domain>/api/auth/login`).

All requests/responses are JSON. All authenticated endpoints expect:
```
Authorization: Bearer <token>
```

## Rate limits

- **60 requests/minute** per token (or per IP if unauthenticated) on most endpoints.
- **10 requests/minute** on `/api/auth/register` and `/api/auth/login` specifically, to slow down credential-stuffing attempts.
- Exceeding the limit returns `429 Too Many Requests` with a JSON `{"error": "..."}` body.

## Getting access

There are two kinds of tokens — use whichever fits:

| | End-user token | Developer API key |
|---|---|---|
| Get it from | `POST /api/auth/login` | `POST /api/auth/api-keys` (while logged in) |
| Lifetime | 24 hours | 1 year |
| Intended for | Your own app's session after a user logs in | A backend integration you control (no interactive login) |

Both work identically everywhere else in the API — just put either one in the `Authorization: Bearer` header.

### 1. Register a user

```
POST /api/auth/register
Content-Type: application/json

{ "name": "Priya Shah", "email": "priya@example.com", "password": "at-least-8-chars" }
```
→ `201 Created`
```json
{ "token": "eyJ...", "userId": "a1b2...", "name": "Priya Shah", "email": "priya@example.com" }
```

### 2. Log in

```
POST /api/auth/login
Content-Type: application/json

{ "email": "priya@example.com", "password": "at-least-8-chars" }
```
→ `200 OK`, same shape as register.

### 3. Generate a developer API key (requires a valid token from step 1 or 2)

```
POST /api/auth/api-keys
Authorization: Bearer <token>
Content-Type: application/json

{ "label": "My integration - production" }
```
→ `201 Created`
```json
{ "id": "...", "label": "My integration - production", "key": "eyJ...", "issuedAt": "...", "expiresAt": "..." }
```
**The `key` is shown once.** Store it securely — there's no way to retrieve it again later (only its list entry, with the last 6 characters, for recognition).

```
GET /api/auth/api-keys            # list your keys (without the secret)
DELETE /api/auth/api-keys/{id}    # remove from your list (see note on revocation in DEPLOY.md)
```

---

## Expenses & categories

All require `Authorization: Bearer <token>`.

```
GET    /api/categories
POST   /api/categories        { "name": "Groceries", "type": "EXPENSE", "icon": "🛒" }
DELETE /api/categories/{id}

GET    /api/expenses
POST   /api/expenses          { "amount": 450.00, "description": "Weekly shop", "categoryId": "...", "type": "EXPENSE", "expenseDate": "2026-07-27" }
PUT    /api/expenses/{id}     (same body as POST)
DELETE /api/expenses/{id}

GET    /api/expenses/summary?year=2026&month=7
       → { "totalIncome": 50000, "totalExpense": 32400, "netBalance": 17600, "periodStart": "...", "periodEnd": "..." }
```
`type` is `"EXPENSE"` or `"INCOME"` throughout.

## Habits

```
GET    /api/habits
POST   /api/habits              { "name": "Read 20 minutes", "frequency": "DAILY" }   # frequency: DAILY | WEEKLY
POST   /api/habits/{id}/complete                                                       # marks today done, updates streak
DELETE /api/habits/{id}
```
Response shape:
```json
{ "id": "...", "name": "Read 20 minutes", "frequency": "DAILY", "currentStreak": 4, "longestStreak": 9, "lastCompletedDate": "2026-07-27" }
```
Calling `/complete` twice on the same day returns `409 Conflict`.

## Tasks

```
GET    /api/tasks
POST   /api/tasks               { "title": "Renew passport", "description": "...", "dueDate": "2026-08-01", "priority": "HIGH" }
PUT    /api/tasks/{id}          (same body as POST)
POST   /api/tasks/{id}/toggle   # flips completed on/off
DELETE /api/tasks/{id}
```
`priority` is `LOW`, `MEDIUM`, or `HIGH`.

---

## Errors

Every error response has this shape:
```json
{ "timestamp": "2026-07-27T10:00:00Z", "status": 404, "error": "Expense not found: <id>" }
```

| Status | Meaning |
|---|---|
| 400 | Validation failed — check the message for which field |
| 401 | Missing/invalid/expired token |
| 404 | Resource doesn't exist, or doesn't belong to you |
| 409 | Conflict (e.g. habit already completed today, email already registered) |
| 429 | Rate limit exceeded |

## Data ownership

Every resource (categories, expenses, habits, tasks) belongs to exactly the user whose token created it — there's no way to see or modify another user's data, and IDs from one user return `404` (not `403`) if queried by another, so you can't even confirm they exist.

## Example: quick integration in curl

```bash
BASE="https://<your-gateway-domain>"

TOKEN=$(curl -s -X POST $BASE/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"priya@example.com","password":"secret123"}' | jq -r .token)

curl -s $BASE/api/expenses/summary -H "Authorization: Bearer $TOKEN" | jq
```
