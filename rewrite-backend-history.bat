@echo off
REM ============================================================
REM Rewrites tally-backend's git history into one commit per file,
REM each with a detailed, professional commit message.
REM
REM Run this from: D:\Coding\Self Project\personal-tracker-backend
REM
REM WARNING: this force-pushes and replaces the current history on
REM GitHub. Safe here since you're the only contributor so far.
REM ============================================================

git checkout --orphan temp-clean-2
git rm -rf --cached . 2>nul

REM ---------- Root / project-wide files ----------

git add pom.xml
git commit -m "chore: add parent Maven POM" -m "Defines the multi-module reactor and manages shared dependency versions (Spring Boot 3.2.5, Spring Cloud 2023.0.1, JJWT 0.11.5) via dependencyManagement, so every service module inherits consistent, centrally-controlled versions instead of duplicating them."

git add .gitignore
git commit -m "chore: add .gitignore for Maven/Java project" -m "Excludes build output (target/), compiled classes, logs, IDE metadata, and local environment override files from version control."

git add .dockerignore
git commit -m "chore: add .dockerignore to scope Docker build context" -m "Keeps the frontend project, build artifacts, and VCS metadata out of the Docker build context sent to each service's Dockerfile, keeping image builds fast and small."

git add docker-compose.yml
git commit -m "chore: add docker-compose for local Postgres" -m "Spins up a single Postgres container for local development, with credentials matching each service's default application.yml settings."

git add init-db.sql
git commit -m "chore: add database initialization script" -m "Runs automatically on first Postgres container startup to create the three per-service databases (auth_db, expense_db, habit_db), mirroring the database-per-service pattern used in production."

git add render.yaml
git commit -m "chore: add Render Blueprint for one-command deployment" -m "Declares the full production topology: one public API gateway, three private backend services (unreachable except through the gateway), and a shared free-tier Postgres instance hosting all three logical databases."

git add render-deploy-setup.sql
git commit -m "chore: add one-time Render database setup script" -m "Creates the second and third logical databases (expense_db, habit_db) on the shared Render Postgres instance, since Render only provisions the one named in render.yaml automatically."

git add DEPLOY.md
git commit -m "docs: add Render deployment guide" -m "Documents the production architecture, one-time setup steps, how end-user tokens and developer API keys both work through the same JWT mechanism, and explicitly calls out free-tier tradeoffs (cold starts, non-distributed rate limiting, stateless API key revocation)."

git add API_REFERENCE.md
git commit -m "docs: add public API reference for third-party developers" -m "Documents every endpoint - authentication, categories, expenses, habits, tasks - with request/response examples, rate limit thresholds, and the standard error response shape, intended as the doc handed to anyone integrating against the API."

git add README.md
git commit -m "docs: add project README with architecture overview" -m "Explains the service boundaries, data-ownership rules, and the two-layer JWT validation strategy (gateway + per-service), plus local setup instructions and links to the deployment and API reference docs."

REM ---------- auth-service ----------

git add auth-service\pom.xml
git commit -m "feat(auth): add module POM" -m "Declares auth-service's dependencies: Spring Web, Spring Data JPA, Spring Security (for BCrypt password hashing), PostgreSQL driver, and JJWT for token issuance."

git add auth-service\src\main\java\com\tracker\auth\AuthServiceApplication.java
git commit -m "feat(auth): add Spring Boot application entry point" -m "Bootstraps the auth-service as a standalone Spring Boot application."

git add auth-service\src\main\java\com\tracker\auth\entity\User.java
git commit -m "feat(auth): add User entity" -m "Defines the persisted user record: id, name, unique email, BCrypt password hash, and creation timestamp. This is the only entity in auth_db that other services never see directly - they only ever receive a validated userId claim."

git add auth-service\src\main\java\com\tracker\auth\entity\ApiKeyRecord.java
git commit -m "feat(auth): add ApiKeyRecord entity" -m "Tracks metadata about long-lived developer API keys a user has issued (label, token suffix for recognition, issued/expiry timestamps). Documents in its class-level javadoc that this table is for user-facing visibility only - actual key validation stays stateless via JWT, so deleting a record here does not revoke the token before it expires."

git add auth-service\src\main\java\com\tracker\auth\repository\UserRepository.java
git commit -m "feat(auth): add UserRepository" -m "Spring Data JPA repository exposing lookups by email, used during both registration (uniqueness check) and login (credential verification)."

git add auth-service\src\main\java\com\tracker\auth\repository\ApiKeyRepository.java
git commit -m "feat(auth): add ApiKeyRepository" -m "Spring Data JPA repository for listing and removing a user's own issued API key records, scoped by userId."

git add auth-service\src\main\java\com\tracker\auth\dto\AuthDtos.java
git commit -m "feat(auth): add request/response DTOs" -m "Defines the record types for registration, login, API key creation, and their responses, including bean validation annotations (email format, minimum password length) enforced at the controller boundary."

git add auth-service\src\main\java\com\tracker\auth\util\JwtUtil.java
git commit -m "feat(auth): add JWT issuance utility" -m "Generates signed JWTs using a shared HMAC secret: short-lived (24h) tokens for normal login sessions, and long-lived (1 year) tokens - tagged with a 'type: api_key' claim - for third-party developer API keys, reusing the exact same signing and validation path as regular user sessions."

git add auth-service\src\main\java\com\tracker\auth\util\JwtAuthFilter.java
git commit -m "feat(auth): add JWT validation filter" -m "A servlet filter that validates incoming bearer tokens and populates the Spring Security context with the authenticated userId, protecting the API key management endpoints - the one part of auth-service that requires the caller to already be logged in."

git add auth-service\src\main\java\com\tracker\auth\util\CurrentUser.java
git commit -m "feat(auth): add CurrentUser helper" -m "Small static helper that reads the authenticated userId out of the Spring Security context, avoiding repetitive boilerplate in services that need to know who's making the request."

git add auth-service\src\main\java\com\tracker\auth\exception\ApiExceptions.java
git commit -m "feat(auth): add domain-specific exceptions" -m "Defines EmailAlreadyExistsException and InvalidCredentialsException, used to produce clear, specific error responses instead of generic 500s."

git add auth-service\src\main\java\com\tracker\auth\exception\GlobalExceptionHandler.java
git commit -m "feat(auth): add global exception handler" -m "Maps domain exceptions and validation failures to consistent JSON error responses with the correct HTTP status codes (409 for conflicts, 401 for bad credentials, 400 for validation errors)."

git add auth-service\src\main\java\com\tracker\auth\config\SecurityConfig.java
git commit -m "feat(auth): add security configuration" -m "Configures Spring Security for a stateless REST API: disables CSRF and sessions, permits registration/login/health checks publicly, and requires a valid JWT for everything else (currently the API key management endpoints)."

git add auth-service\src\main\java\com\tracker\auth\service\AuthService.java
git commit -m "feat(auth): add registration and login business logic" -m "Handles the core auth flow: checks email uniqueness and hashes passwords on registration, verifies credentials with constant-time BCrypt comparison on login, and issues a signed JWT in both cases."

git add auth-service\src\main\java\com\tracker\auth\service\ApiKeyService.java
git commit -m "feat(auth): add API key management business logic" -m "Generates 1-year developer API keys tied to the requesting user's identity, and provides listing/deletion of the user's own key records. Deletion is documented as removing visibility only, not revoking the underlying stateless token."

git add auth-service\src\main\java\com\tracker\auth\controller\AuthController.java
git commit -m "feat(auth): add registration and login endpoints" -m "Exposes POST /api/auth/register and POST /api/auth/login, the two public entry points into the entire system."

git add auth-service\src\main\java\com\tracker\auth\controller\ApiKeyController.java
git commit -m "feat(auth): add API key management endpoints" -m "Exposes POST/GET/DELETE under /api/auth/api-keys, letting a logged-in user generate a long-lived developer key, list their existing keys (secret never shown again after creation), and remove one from their list."

git add auth-service\src\main\resources\application.yml
git commit -m "feat(auth): add service configuration" -m "Configures the datasource connection (host/port/db name resolved from environment variables for portability between local Docker and Render), JWT signing secret and expiry, and exposed actuator health endpoint."

git add auth-service\Dockerfile
git commit -m "chore(auth): add Dockerfile" -m "Multi-stage build: compiles the module with Maven against the shared parent POM, then copies just the built JAR into a slim JRE runtime image for a smaller, faster-starting container."

REM ---------- expense-service ----------

git add expense-service\pom.xml
git commit -m "feat(expense): add module POM" -m "Declares expense-service's dependencies: Spring Web, Spring Data JPA, Spring Security (for JWT-based request authentication), PostgreSQL driver, and JJWT for independent token validation."

git add expense-service\src\main\java\com\tracker\expense\ExpenseServiceApplication.java
git commit -m "feat(expense): add Spring Boot application entry point" -m "Bootstraps the expense-service as a standalone Spring Boot application."

git add expense-service\src\main\java\com\tracker\expense\entity\Category.java
git commit -m "feat(expense): add Category entity" -m "Represents a user-defined expense or income category (name, type, icon), scoped by a userId trusted from the validated JWT rather than a foreign key, since the user record itself lives in a different service's database."

git add expense-service\src\main\java\com\tracker\expense\entity\Expense.java
git commit -m "feat(expense): add Expense entity" -m "Represents a single income or expense entry: amount, description, category reference, type, and date, indexed on (userId, expenseDate) to keep per-user history queries and monthly aggregations fast."

git add expense-service\src\main\java\com\tracker\expense\repository\CategoryRepository.java
git commit -m "feat(expense): add CategoryRepository" -m "Spring Data JPA repository scoping all category lookups to the owning user."

git add expense-service\src\main\java\com\tracker\expense\repository\ExpenseRepository.java
git commit -m "feat(expense): add ExpenseRepository with summary query" -m "Provides per-user expense lookups plus a custom JPQL aggregation query that sums amounts by type (income/expense) within a date range, backing the monthly summary endpoint."

git add expense-service\src\main\java\com\tracker\expense\security\JwtAuthFilter.java
git commit -m "feat(expense): add JWT validation filter" -m "Validates the bearer token independently of the API gateway (defense in depth) and populates the Spring Security context with the authenticated userId, so this service never trusts the network alone."

git add expense-service\src\main\java\com\tracker\expense\security\CurrentUser.java
git commit -m "feat(expense): add CurrentUser helper" -m "Reads the authenticated userId from the Spring Security context for use in service-layer queries and writes."

git add expense-service\src\main\java\com\tracker\expense\config\SecurityConfig.java
git commit -m "feat(expense): add security configuration" -m "Configures a stateless security filter chain requiring a valid JWT on every endpoint except health checks."

git add expense-service\src\main\java\com\tracker\expense\dto\ExpenseDtos.java
git commit -m "feat(expense): add request/response DTOs" -m "Defines record types for category and expense requests/responses plus the monthly summary response shape, with bean validation on amounts, descriptions, and required fields."

git add expense-service\src\main\java\com\tracker\expense\exception\ApiExceptions.java
git commit -m "feat(expense): add domain-specific exceptions" -m "Defines ResourceNotFoundException, used whenever a requested category or expense doesn't exist or doesn't belong to the caller."

git add expense-service\src\main\java\com\tracker\expense\exception\GlobalExceptionHandler.java
git commit -m "feat(expense): add global exception handler" -m "Maps not-found and validation exceptions to consistent JSON error responses with appropriate HTTP status codes."

git add expense-service\src\main\java\com\tracker\expense\service\CategoryService.java
git commit -m "feat(expense): add category management business logic" -m "Handles creating, listing, and deleting categories, enforcing that every operation is scoped to the authenticated user."

git add expense-service\src\main\java\com\tracker\expense\service\ExpenseService.java
git commit -m "feat(expense): add expense tracking business logic" -m "Handles full CRUD for expense/income entries and computes monthly summaries (total income, total expense, net balance) for a given year/month, defaulting to the current month when unspecified."

git add expense-service\src\main\java\com\tracker\expense\controller\CategoryController.java
git commit -m "feat(expense): add category endpoints" -m "Exposes GET/POST/DELETE under /api/categories."

git add expense-service\src\main\java\com\tracker\expense\controller\ExpenseController.java
git commit -m "feat(expense): add expense endpoints" -m "Exposes GET/POST/PUT/DELETE under /api/expenses, plus GET /api/expenses/summary for the monthly income/expense/balance breakdown."

git add expense-service\src\main\resources\application.yml
git commit -m "feat(expense): add service configuration" -m "Configures the datasource connection via environment variables, JWT secret shared with the other services, and exposed health endpoint."

git add expense-service\Dockerfile
git commit -m "chore(expense): add Dockerfile" -m "Multi-stage build producing a slim runtime image, consistent with the other services' Docker setup."

REM ---------- habit-service ----------

git add habit-service\pom.xml
git commit -m "feat(habit): add module POM" -m "Declares habit-service's dependencies, mirroring expense-service's setup: Spring Web, Spring Data JPA, Spring Security, PostgreSQL driver, and JJWT."

git add habit-service\src\main\java\com\tracker\habit\HabitServiceApplication.java
git commit -m "feat(habit): add Spring Boot application entry point" -m "Bootstraps the habit-service as a standalone Spring Boot application."

git add habit-service\src\main\java\com\tracker\habit\entity\Habit.java
git commit -m "feat(habit): add Habit entity" -m "Represents a recurring habit with its frequency (daily/weekly), current and longest streak counters, and the date it was last completed - the fields the streak-calculation logic reads and updates."

git add habit-service\src\main\java\com\tracker\habit\entity\HabitLog.java
git commit -m "feat(habit): add HabitLog entity" -m "Records individual completion events per habit per day, with a unique constraint on (habitId, completedDate) preventing the same day from being logged twice at the database level."

git add habit-service\src\main\java\com\tracker\habit\entity\Task.java
git commit -m "feat(habit): add Task entity" -m "Represents a one-off task independent of the recurring habit model: title, optional description, due date, priority, and a simple completed flag."

git add habit-service\src\main\java\com\tracker\habit\repository\HabitRepository.java
git commit -m "feat(habit): add HabitRepository" -m "Spring Data JPA repository scoping habit lookups to the owning user."

git add habit-service\src\main\java\com\tracker\habit\repository\HabitLogRepository.java
git commit -m "feat(habit): add HabitLogRepository" -m "Supports checking whether a habit was already completed on a given date, the guard that prevents double-counting a streak within the same day."

git add habit-service\src\main\java\com\tracker\habit\repository\TaskRepository.java
git commit -m "feat(habit): add TaskRepository" -m "Spring Data JPA repository scoping task lookups to the owning user, ordered by due date."

git add habit-service\src\main\java\com\tracker\habit\security\JwtAuthFilter.java
git commit -m "feat(habit): add JWT validation filter" -m "Validates the bearer token independently of the API gateway and populates the Spring Security context with the authenticated userId."

git add habit-service\src\main\java\com\tracker\habit\security\CurrentUser.java
git commit -m "feat(habit): add CurrentUser helper" -m "Reads the authenticated userId from the Spring Security context for use across habit and task operations."

git add habit-service\src\main\java\com\tracker\habit\config\SecurityConfig.java
git commit -m "feat(habit): add security configuration" -m "Configures a stateless security filter chain requiring a valid JWT on every endpoint except health checks."

git add habit-service\src\main\java\com\tracker\habit\exception\ApiExceptions.java
git commit -m "feat(habit): add domain-specific exceptions" -m "Defines ResourceNotFoundException and AlreadyCompletedException, the latter backing the rule that a habit can only be marked complete once per day."

git add habit-service\src\main\java\com\tracker\habit\exception\GlobalExceptionHandler.java
git commit -m "feat(habit): add global exception handler" -m "Maps not-found, already-completed, and validation exceptions to consistent JSON error responses with appropriate HTTP status codes (404, 409, 400 respectively)."

git add habit-service\src\main\java\com\tracker\habit\dto\HabitDtos.java
git commit -m "feat(habit): add request/response DTOs" -m "Defines record types for habit and task requests/responses, including streak fields in the habit response shape."

git add habit-service\src\main\java\com\tracker\habit\service\HabitService.java
git commit -m "feat(habit): add habit tracking and streak calculation logic" -m "Implements the core streak rule: completing a habit the day after the last completion increments the streak, any gap resets it to 1, and longestStreak is updated whenever the current streak surpasses it. Rejects a second completion on the same day with a 409."

git add habit-service\src\main\java\com\tracker\habit\service\TaskService.java
git commit -m "feat(habit): add task management business logic" -m "Handles full CRUD for one-off tasks and a toggle action for flipping completion status."

git add habit-service\src\main\java\com\tracker\habit\controller\HabitController.java
git commit -m "feat(habit): add habit endpoints" -m "Exposes GET/POST/DELETE under /api/habits plus POST /api/habits/{id}/complete for marking today's completion."

git add habit-service\src\main\java\com\tracker\habit\controller\TaskController.java
git commit -m "feat(habit): add task endpoints" -m "Exposes GET/POST/PUT/DELETE under /api/tasks plus POST /api/tasks/{id}/toggle for flipping completion status."

git add habit-service\src\main\resources\application.yml
git commit -m "feat(habit): add service configuration" -m "Configures the datasource connection via environment variables, shared JWT secret, and exposed health endpoint."

git add habit-service\Dockerfile
git commit -m "chore(habit): add Dockerfile" -m "Multi-stage build producing a slim runtime image, consistent with the other services' Docker setup."

REM ---------- api-gateway ----------

git add api-gateway\pom.xml
git commit -m "feat(gateway): add module POM" -m "Declares api-gateway's dependencies: Spring Cloud Gateway for reactive routing, and JJWT for validating tokens before requests are forwarded downstream."

git add api-gateway\src\main\java\com\tracker\gateway\ApiGatewayApplication.java
git commit -m "feat(gateway): add Spring Boot application entry point" -m "Bootstraps the api-gateway as a standalone Spring Boot application - the single public entry point into the whole system."

git add api-gateway\src\main\java\com\tracker\gateway\config\JwtAuthenticationFilter.java
git commit -m "feat(gateway): add JWT validation and forwarding filter" -m "Validates the bearer token on every request except registration, login, and health checks, rejecting invalid/expired tokens with 401 before they ever reach a backend service, and forwards the extracted userId downstream via an X-User-Id header."

git add api-gateway\src\main\java\com\tracker\gateway\config\CorsConfig.java
git commit -m "feat(gateway): add CORS configuration" -m "Enables cross-origin requests so browser-based frontends - including third-party developer integrations, not just the first-party client - can call the API directly, with allowed origins configurable via an environment variable for tightening later."

git add api-gateway\src\main\java\com\tracker\gateway\config\RateLimitFilter.java
git commit -m "feat(gateway): add per-client rate limiting" -m "Applies a fixed-window in-memory rate limit keyed by bearer token (or IP for unauthenticated requests), with a stricter limit on login/register to slow credential-stuffing attempts. Documented as non-distributed - sufficient for a single instance, but noted as needing a Redis-backed replacement before horizontal scaling."

git add api-gateway\src\main\resources\application.yml
git commit -m "feat(gateway): add routing and service configuration" -m "Defines static route mappings to each backend service resolved from environment variables (rather than service discovery, which is unnecessary once every service has a stable host on its deployment platform), plus configurable CORS origins and rate limit thresholds."

git add api-gateway\Dockerfile
git commit -m "chore(gateway): add Dockerfile" -m "Multi-stage build producing a slim runtime image for the gateway, consistent with the other services' Docker setup."

REM ---------- catch-all ----------

git add .
git commit -m "chore: add any remaining project files" --allow-empty

git branch -D main
git branch -m main
git push -f origin main

echo.
echo Done. Run "git log --oneline" to review the full commit history.