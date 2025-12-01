# MINDSOCCER Backend

Backend Spring Boot 3 pour le jeu de culture générale MINDSOCCER.

## Stack technique

- **Java 21** + **Spring Boot 3.3**
- **PostgreSQL 16** - Base de données principale
- **Redis 7** - Timers, locks, pub/sub temps réel
- **MinIO/S3** - Stockage médias
- **WebSocket/STOMP** - Communication temps réel

## Structure

```
mindsoccer-backend/
├── apps/api/                   # Application Spring Boot
├── modules/
│   ├── protocol/               # DTOs, enums, contrats
│   ├── engine/                 # Moteur + plugins par rubrique
│   ├── match/                  # Orchestration matchs
│   ├── realtime/               # WebSocket/STOMP
│   ├── content/                # Questions/FOCUS
│   ├── scoring/                # Points, pénalités
│   ├── anticheat/              # Rate-limit, journal
│   └── shared/                 # Utilitaires
└── infra/                      # Docker, SQL
```

## Démarrage

```bash
# Infrastructure
cd infra && docker compose up -d

# Application
cp .env.example .env
./gradlew :apps:api:bootRun
```

- API: http://localhost:8080
- Swagger: http://localhost:8080/swagger-ui.html
- Health: http://localhost:8080/actuator/health

## Build

```bash
./gradlew :apps:api:bootJar
java -jar apps/api/build/libs/mindsoccer-api.jar
```
