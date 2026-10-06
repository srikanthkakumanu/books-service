# books-service

The catalog context of the platform: **books and their authors**. It is the first business service that runs on the identity platform, and it relies on that platform for everything about people:

- **Users** are the ones [`user-service`](../user-service/README.md) manages. This service stores no user data; a book only records the platform user ID of its owner.
- **Login** happens at [`auth-service`](../auth-service/README.md). Nothing here is usable without a platform access token.
- **Roles** are created and assigned in `auth-service`. This service defines none and checks only the permissions that arrive in the token.

## Contents

- [Responsibilities](#responsibilities)
- [Who may do what](#who-may-do-what)
- [API](#api)
- [Errors](#errors)
- [Rules the service enforces](#rules-the-service-enforces)
- [Architecture](#architecture)
- [Data](#data)
- [Seed data](#seed-data)
- [Configuration](#configuration)
- [Dev users and passwords](#dev-users-and-passwords)
- [Run](#run)
- [Test](#test)
- [Build and image](#build-and-image)

## Responsibilities

| Area | What it does |
| --- | --- |
| Books | Add, read, search, change, remove; every book has an author and may have an owner |
| Authors | Add, read, search, change, remove |
| Ownership | A book belongs to the user who added it; a catalog manager can give it to another user |
| Starter catalog | 47 authors and 44 books loaded at startup in development |

## Who may do what

Being logged in is not enough: a caller needs a catalog role. A user with only the platform's default `USER` role gets `403`.

| Role (managed in auth-service) | Permissions in the token | May |
| --- | --- | --- |
| `CATALOG_READER` | `books:read` | read books and authors |
| `CATALOG_EDITOR` | `books:read`, `books:write` | also add books, and change or remove their own |
| `CATALOG_MANAGER` | `books:read`, `books:write`, `books:manage`, `authors:manage` | also change or remove any book, transfer ownership, manage authors |
| `PLATFORM_ADMIN` | all four | everything |

Give someone a role through the platform, never here:

```bash
curl -s -X POST localhost:9211/api/v1/users/$USER_ID/roles -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H 'Content-Type: application/json' -d '{"roles":[{"name":"CATALOG_EDITOR"}]}'
```

The user's next token (after a refresh or a new login) carries the permissions. The roles, the permissions and the `books-service` client itself are created through the platform APIs by the onboarding job in [`micro-services`](../micro-services/README.md) (`onboarding/books-service.json`).

## API

Base paths `/api/v1/books` and `/api/v1/authors`. Reach them through the gateway (`http://localhost:9211`); the service itself listens on 9151. OpenAPI at `/v3/api-docs`, Swagger UI at `/swagger-ui.html` (off in `prod`).

Every call needs `Authorization: Bearer <access token>` from `POST /api/v1/auth/login`.

### Books

| Method and path | Purpose | Needs |
| --- | --- | --- |
| `GET /api/v1/books` | Search. Filters: `title`, `publisher` (contain, any case), `isbn`, `authorId`, `ownerId`, `owner=me`. Paging: `page`, `size`, `sort`. | `books:read` |
| `GET /api/v1/books/{id}` | Read a book | `books:read` |
| `POST /api/v1/books` | Add a book. `201` with `Location`. It belongs to the caller; a manager may name another `ownerId`. | `books:write` |
| `PUT /api/v1/books/{id}` | Replace its details | `books:write`, and owner or `books:manage` |
| `PUT /api/v1/books/{id}/owner` | Give it to another active platform user: `{"ownerId": "..."}` | `books:manage` |
| `DELETE /api/v1/books/{id}` | Remove it. `204`. | `books:write`, and owner or `books:manage` |

### Authors

| Method and path | Purpose | Needs |
| --- | --- | --- |
| `GET /api/v1/authors` | Search. Filters: `name` (first or last, contains), `genre`. Paging: `page`, `size`, `sort`. | `books:read` |
| `GET /api/v1/authors/{id}` | Read an author | `books:read` |
| `POST /api/v1/authors` | Add an author. `201` with `Location`. | `authors:manage` |
| `PUT /api/v1/authors/{id}` | Replace name and genre | `authors:manage` |
| `DELETE /api/v1/authors/{id}` | Remove an author who has no books. `204`. | `authors:manage` |

### Bodies

```json
// POST or PUT /api/v1/books
{"title": "Dune", "description": "A desert planet.", "isbn": "978-0-441-17271-9", "publisher": "Ace",
 "authorId": "<author id>", "completed": false}

// response
{"id": "...", "title": "Dune", "description": "A desert planet.", "isbn": "9780441172719", "publisher": "Ace",
 "author": {"id": "...", "firstName": "Frank", "lastName": "Herbert"}, "ownerId": "<platform user id or null>",
 "completed": false, "createdAt": "...", "updatedAt": "..."}

// POST or PUT /api/v1/authors
{"firstName": "Frank", "lastName": "Herbert", "genre": "Science Fiction"}
```

Lists return `items`, `total`, `page`, `size`. `page` starts at 0; `size` is 1 to 100 (default 20). `sort` is `field` or `field,asc|desc`: books by `title` (default), `publisher`, `createdAt`; authors by `lastName` (default), `firstName`, `genre`, `createdAt`.

### Example

```bash
. ../micro-services/.env
TOKEN=$(curl -s -X POST localhost:9211/api/v1/auth/login -H 'Content-Type: application/json' \
  -d "{\"username\":\"platform-admin\",\"password\":\"$PLATFORM_ADMIN_PASSWORD\"}" | jq -r .accessToken)

curl -s "localhost:9211/api/v1/books?size=3&sort=title" -H "Authorization: Bearer $TOKEN" | jq
curl -s "localhost:9211/api/v1/authors?genre=mystery" -H "Authorization: Bearer $TOKEN" | jq '.items[].lastName'
```

## Errors

Every error is an RFC 9457 problem (`application/problem+json`) with a stable `type` (`https://platform.local/problems/<code>`) and a matching `code`. Clients should switch on `code`, never on the text.

| `code` | Status | When |
| --- | --- | --- |
| `invalid-value` | 400 | Validation failed; `errors` lists `field` and `message`. Includes an ISBN with a wrong check digit and an `authorId` that does not exist. |
| `unauthorized` | 401 | No token, or one that cannot be verified or is not meant for this service |
| `forbidden` | 403 | The token lacks the permission |
| `operation-not-permitted` | 403 | A catalog rule refused it, for example changing someone else's book |
| `book-not-found`, `author-not-found` | 404 | |
| `duplicate-book` | 409 | Another book already has the ISBN |
| `author-in-use` | 409 | The author still has books |
| `owner-not-eligible` | 422 | The user a book is to be given to does not exist or is not active |
| `user-directory-unavailable` | 503 | user-service or auth-service could not be asked about a user |
| `internal-error` | 500 | Anything unexpected |

## Rules the service enforces

Domain rules, tested without Spring:

- **A book belongs to whoever adds it.** Only a catalog manager may add a book for someone else.
- **Only the owner or a catalog manager changes or removes a book.** Another editor cannot.
- **Catalog-owned books** (no owner, such as the starter catalog) are changed only by a catalog manager.
- **Only a catalog manager transfers a book**, even the owner cannot, and only to a user that user-service reports as active.
- **An ISBN is an ISBN-13 with a correct check digit**, and no two books share one. Hyphens and spaces are accepted and dropped.
- **A book names an existing author**, and an author who still has books cannot be removed.

Things to know:

- Ordinary requests need no call to another service: identity and permissions come from the token. Only giving a book to another user asks user-service.
- A revoked role or a logout takes effect here when the access token expires or is refreshed (about five minutes in dev).
- When a user is deleted in user-service their books keep the old owner ID; a catalog manager can reassign or remove them.

## Architecture

Clean architecture; dependencies point inward and ArchUnit fails the build if they do not.

```
com.books
├── domain            Pure Java. No Spring, JPA or HTTP.
│   ├── model         Book, Author, BookDetails, BookId, AuthorId, OwnerId, Title, Isbn, Publisher,
│   │                 PersonName, Genre, CatalogActor, BookSearch, AuthorSearch, Paging, PageResult
│   ├── port          BookRepository, AuthorRepository, UserDirectoryPort
│   └── exception     One type per error code
├── application       One class per use case: CreateBook, UpdateBook, DeleteBook, TransferBook, GetBook,
│                     SearchBooks, CreateAuthor, UpdateAuthor, DeleteAuthor, GetAuthor, SearchAuthors,
│                     SeedCatalog. Depends only on domain.
├── infrastructure
│   ├── persistence   JPA entities and repositories
│   ├── platform      The only code that calls user-service and auth-service
│   ├── seed          Reads the seed files and runs SeedCatalog at startup
│   └── config        Wires the use cases as beans
└── interfaces
    ├── rest          Controllers, request and response models, problem-detail error handling
    └── security      Filter chain, permission expressions, the caller as the domain sees them
```

- The domain's view of the caller, `CatalogActor`, is built from the validated token: the user ID from `sub`, and whether they manage every book from the `books:manage` permission.
- `UserDirectoryPort` is the domain's only knowledge of users: "what is this user's standing?". Its adapter obtains a client-credentials token from auth-service (`POST /api/v1/auth/service-token`), calls `GET /api/v1/users/{id}` on user-service through the registry, caches the token until shortly before it expires, and gets a new one once if it is refused.
- Tokens are validated with the shared `platform-security-starter` from `micro-services`: signature against the JWKS, RS256 only, exact issuer, `books-service` in the audience, expiry with clock skew, `typ` `Bearer`.

## Data

Database `booksdb` in the platform's Postgres. Flyway owns the schema (`src/main/resources/db/migration`); Hibernate only validates it.

| Table | Columns |
| --- | --- |
| `author` | `id`, `first_name`, `last_name` (optional), `genre`, `created_at`, `updated_at`, `version` |
| `book` | `id`, `title`, `description` (optional), `isbn` (unique), `publisher`, `author_id` (foreign key), `owner_id` (platform user ID, or null), `completed`, `created_at`, `updated_at`, `version` |

Two database accounts, as before:

| Account | Role | Used for |
| --- | --- | --- |
| `booksadmin` | owns the schema | Flyway migrations at startup |
| `theuser` | reads and writes rows; cannot create, alter or drop tables | everything else |

The database and both accounts are created by the platform's database job with credentials read from Vault.

## Seed data

`src/main/resources/data/authors.json` (47 authors) and `books.json` (44 books), shaped like the model:

```json
{"id": "9d26b580-...", "title": "The Enigma of Elysium", "description": "A mystery title by Evelyn Wren, published by Mystic Press.",
 "isbn": "9781234567897", "publisher": "Mystic Press", "authorId": "a1fcc431-..."}
```

- Every entry has a fixed ID, every book names its author, and every ISBN is a valid ISBN-13.
- Entries go through the same domain objects as API requests, so a seed file that breaks a rule stops the service at startup instead of loading bad data.
- Loading is safe to repeat: an entry whose ID is already stored is left alone, so later edits survive a restart.
- Seeded books have no owner; only a catalog manager can change them.
- It runs when `books.seed.enabled` is `true`: on in `dev`, off in `qa` and `prod` (set in `service-configs`).

## Configuration

Split by environment ([ADR 0014](../micro-services/docs/adr/0014-environment-profiles.md)):

| File | Holds |
| --- | --- |
| `application.yml` | What is common: name, port, JPA settings, graceful shutdown, client ID, audience |
| `application-dev.yml` | Config Server and Vault imports, optional, with localhost defaults |
| `application-qa.yml`, `application-prod.yml` | The same imports, required, with no defaults |

More settings come from the Config Server (`service-configs/application*.yml` and `books-service*.yml`) and secrets from Vault.

| Variable | Default in `dev` | Meaning |
| --- | --- | --- |
| `SPRING_PROFILES_ACTIVE` | `dev` | `dev`, `qa` or `prod` |
| `SERVER_PORT` | `9151` | HTTP port |
| `CONFIG_SERVER_URL` | `http://localhost:9311` | Config Server |
| `VAULT_URI`, `VAULT_TOKEN` | `http://localhost:8200`, none | Vault and this service's token |
| `DATABASE_URL` | `jdbc:postgresql://localhost:5432/booksdb` | Its database |
| `KEYCLOAK_URL` | `http://localhost:8080` | Where it fetches signing keys |
| `KEYCLOAK_PUBLIC_URL` | `http://localhost:8080` | The issuer in tokens; compared exactly |
| `EUREKA_URL` | `http://localhost:9111/eureka/` | Registry |

Other settings: `books.seed.enabled`; `platform.directory.user-service-url` and `auth-service-url` (service names resolved through the registry by default), `platform.directory.load-balanced`, `connect-timeout`, `read-timeout`.

No credential is in any file of this repository. From Vault:

| Vault path | Keys | Written by |
| --- | --- | --- |
| `secret/books-service` | `spring.datasource.username`, `spring.datasource.password` (runtime account), `spring.flyway.user`, `spring.flyway.password` (schema admin) | the platform's Vault seeding job |
| `secret/clients/books-service` | `client-secret` (for its own service token) | auth-service, when the client is registered or its secret renewed |

## Dev users and passwords

For the development environment only.

| Account | User | Password | Where it is kept |
| --- | --- | --- | --- |
| `booksdb` schema admin | `booksadmin` | `booksadmin` | Vault `secret/books-service` (`spring.flyway.*`) |
| `booksdb` runtime | `theuser` | `theuser` | Vault `secret/books-service` (`spring.datasource.*`) |
| Vault dev root token | | `srikanth` | `micro-services/.env.dev.example` |
| Platform administrator (holds every catalog permission) | `platform-admin` | generated; `PLATFORM_ADMIN_PASSWORD` in `micro-services/.env` | Vault `secret/keycloak` |
| This service's Vault token | | generated; `BOOKS_SERVICE_VAULT_TOKEN` in `micro-services/.env` | |
| This service's client secret | `books-service` | generated by auth-service | Vault `secret/clients/books-service` |

There are no application users of this service's own: sign in with a platform user. The full table for the platform is in the [`micro-services` README](../micro-services/README.md#dev-users-and-passwords).

```bash
docker compose -f ../micro-services/docker-compose.yml exec -e PGPASSWORD=theuser postgres \
  psql -h localhost -U theuser -d booksdb -c 'select count(*) from book'
```

## Run

This repository must sit next to [`micro-services`](../micro-services/README.md), which holds the version catalog and the shared starter.

**With the whole platform** (the usual way):

```bash
cd ../micro-services && make up
```

books-service starts last: after the gateway is up and the onboarding job has registered it with the platform.

**From source, against the running platform:**

```bash
cd ../micro-services && scripts/run-from-source.sh books-service
```

**Restart just this service** after a change: `cd ../micro-services && scripts/restart.sh --build books-service`.

The service shuts down gracefully: on stop it finishes requests in flight (up to 30 seconds) and deregisters from Eureka.

## Test

```bash
./gradlew build
```

Needs Docker for Testcontainers. 92 tests; none are skipped.

| Kind | Tests | Against |
| --- | --- | --- |
| Domain | 28 | Plain Java: value objects, ownership rules |
| Use cases | 14 | In-memory ports, including the seed loader |
| Architecture (ArchUnit) | 7 rules | The compiled classes |
| Repositories | 9 | Postgres 18, schema from Flyway: constraints, search, paging |
| User lookup adapter | 10 | A stand-in for auth-service and user-service over HTTP |
| Controllers (`@WebMvcTest`) | 18 | Mocked use cases: validation, error mapping, authorization |
| Whole service | 6 | Postgres, the real seed files, tokens verified against a JWKS endpoint |

The build fails if line coverage of `domain` and `application` drops below 80% (currently 99%). Reports: `build/reports/tests/test/index.html` and `build/reports/jacoco/test/html/index.html`.

The end-to-end tests are in `micro-services` (`BooksE2ETest`, run with `make test-e2e`): users are created through user-service, roles assigned through auth-service, and the catalog is used through the gateway.

## Build and image

- Java 27, Gradle 9.8.0 (wrapper), Spring Boot 4.1.1. Versions come from `../micro-services/gradle/libs.versions.toml`.
- `Dockerfile` is multi-stage: build on JDK 27, run on a JRE 27 Alpine image as a non-root user, with a health check on `/actuator/health/readiness`. It needs the platform root as a named build context, which `micro-services/docker-compose.yml` supplies:

```bash
docker build --build-context platform=../micro-services -t books-service .
```

- CI (`.github/workflows/build.yml`) checks out `micro-services` next to this repository, runs `./gradlew build` and builds the image.
- `bin/verify-image.rb` is a smoke test written for the previous implementation (its own Keycloak realm, `/api/books/ping`). It does not match this service any more and is not run by CI; the checks it made are covered by the tests above and by the platform's end-to-end suite.
