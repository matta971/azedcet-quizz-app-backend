# **MINDSOCCER — Concept exhaustif & Architecture à deux dépôts**

**Back : Spring Boot 3 (Java 21\)** • **Front : Next.js \+ React Native (Expo)**

## **1\) Vision & philosophie**

**MINDSOCCER** est un jeu de culture générale à tonalité sportive. Deux équipes s’affrontent via des **rubriques** (“manches”) inspirées de sports, avec **timers**, **bonus**, **pénalités**, **enchères**, **échelles** et **indices**. Objectifs : esprit d’équipe, gestion du stress, respect de l’adversaire, plaisir d’apprendre. Slogan : **« Enclaver les océans du savoir »**.

---

## **2\) Rôles & formats**

* **Équipes** : A et B (duo ou équipes 3–5).

* **Arbitre (host)** : cadence les rubriques, annonce « Top \! », lit, valide, applique bonus/pénalités, gère droits de réplique.

* **Joueurs** : répondent selon la rubrique (individuel, relais, duel, échelle…).

* **Public** (optionnel en ligne) : spectateurs, replays, caster.

---

## **3\) Règles transverses (communes)**

* **Pénalité** concédée ⇒ **question bonus** à l’adversaire.

* **5 pénalités** pour un joueur ⇒ **suspension** : absence sur **4×10 pts** ou **1×40 pts**.

* **Horloge serveur** \= source de vérité (et non celle du client).

* **Droit de réplique** selon la rubrique, **idempotence** des actions.

---

## **4\) Rubriques — règles détaillées**

* **SMASH A / SMASH B** : l’équipe pose une **question vérifiable** pour « coller » l’autre. **3 s** pour **annoncer** la question après « Top \! ». Dépassé ? **\+10 pts** à l’adverse. Concertation autorisée sur SMASH A.

* **CASCADE** : **10 questions éclairs**. Questions **collectives**, **réponses individuelles** (le plus rapide parle). **Droit de réplique** possible.

* **PANIER** (4 thèmes) : l’équipe **en tête** choisit **1 thème** (annonce **≤ 3 s**) et désigne un **tireur** (répond **seul** à 4 Q). Score : **10 × bonnes**.

* **RELAIS** : l’équipe **menée** prend un **thème restant** ; **sans faute** et **≤ 40 s** ⇒ **\+20 pts** bonus.

* **DUEL** (langue) : **1 v 1** (expressions, grammaire, étymologie…).

* **SAUT PATRIOTIQUE** : **4 Q** sur le **pays** de l’équipe qui mène (ou du capitaine si multi-nationalité). Après lecture : « En voulez-vous ? » → **Oui/Non**. **Une mauvaise annule une bonne**. Si *Non*, l’équipe menée peut répondre.

* **ÉCHAPPÉE** : réussir la **clé de continent** pour voyager (monuments, villes, célébrités…) sur ce continent.

* **ESTOCADE** : **3 Q**, **chacune \= 40 pts** (1 indice max).

* **MARATHON** : **10 Q** d'endurance.

**--- 2ème Partie ---**

* **JACKPOT** : **enchères** avec **cagnotte 100 pts**/équipe sur **3 Q** à **indices successifs** (du plus flou au plus net). Gagnant de l'enchère répond ; **erreur ⇒ mise à l'adverse**. Égalité au 3ᵉ indice : **éclair** valant la mise.

* **TRANSALT** : **10 questions éclairs** sur les **villes et pays du monde** pour voyager.

* **CROSS-COUNTRY** : **10 questions** sur des **lieux géographiques** (villes, monuments, pays).

* **CROSS-DICTIONARY** : **4 questions** sur des **noms propres** (personnes, lieux) commençant par une **lettre imposée**.

* **TIRS AU BUT** : découvrir **un mot** (les « gardiens » tirent aussi). **Victoire de séance \= \+40 pts**. Égalité : **3 éclairs**.

* **CAPOEIRA** (musique) : l’équipe en tête **défie** des joueurs adverses sur des **Q musicales**.

* **CIME** : **échelle de 10 Q**, **3 jokers** (les 3 coéquipiers, 1 fois chacun) ; le joker **dit** sa réponse, le joueur **n’est pas obligé** de suivre. À chaque bonne, on **quitte** ou on **double**. **\>4** bonnes ⇒ **\+20** ; **\>7** ⇒ **\+40**. **30 s** de **négociation** (capitaines, 3 thèmes proposés).

* **RANDONNÉE LEXICALE** : **10 Q**, **noms communs uniquement**. Avance **A→Z** sans retour.

* **IDENTIFICATION** : **1 Q** avec **4 indices** valant **40 / 30 / 20 / 10**.

* **SPRINT FINAL** : **20 éclairs** pour confirmer/renverser la hiérarchie.

---

## **5\) Architecture à deux dépôts (repos)**

### **5.1 Vue d’ensemble**

* **Repo A : `mindsoccer-backend` (Spring Boot 3, Java 21\)**  
   API REST, WebSocket/STOMP, moteur d’états « plugins de règles », scoring/pénalités, contenus (questions/FOCUS), anti-triche, observabilité, sécurité.

* **Repo B : `mindsoccer-frontend` (monorepo JS/TS)**

  * **Web** : Next.js (React) – landing/SEO, règles publiques, spectateur, studio d’auteurs, admin.

  * **Mobile** : React Native (Expo) – lobby → match → score, notifications, OTA.

  * **Packages partagés** : design system, client API (SDK généré), types.

---

## **6\) `mindsoccer-backend` — Spring Boot**

### **6.1 Arborescence multi-modules (Gradle)**

`mindsoccer-backend/`  
`├─ settings.gradle`  
`├─ build.gradle`  
`├─ gradle/`  
`├─ apps/`  
`│  ├─ gateway/                 # (optionnel) Spring Cloud Gateway`  
`│  └─ api/                     # Spring Boot app "tout-en-un" (REST+WS)`  
`├─ modules/`  
`│  ├─ protocol/                # DTO, enums, erreurs, OpenAPI source`  
`│  ├─ engine/                  # Moteur d’états + plugins par rubrique`  
`│  ├─ match/                   # Orchestration match/rounds (REST façade)`  
`│  ├─ realtime/                # WS/STOMP, rooms, tick horloge`  
`│  ├─ content/                 # Questions/FOCUS (CRUD, import CSV/Excel)`  
`│  ├─ scoring/                 # Calculs points, pénalités, suspensions`  
`│  ├─ anti-cheat/              # Heuristiques + journal append-only`  
`│  └─ shared/                  # i18n, time, idempotence, exceptions, utils`  
`└─ infra/`  
   `├─ docker-compose.yml       # postgres, redis, minio en dev`  
   `├─ k8s/                     # manifests`  
   `└─ helm/                    # charts (prod)`

**Option** : démarrer avec une **app unique** `apps/api` qui assemble les modules ; migrer vers microservices plus tard si besoin.

### **6.2 Stack & libs**

* **Spring Boot 3 / Java 21**

* **WebFlux \+ Reactor Netty** (HTTP non bloquant)

* **WebSocket / STOMP** (Spring Messaging)

* **Spring Security 6** (JWT \+ refresh, RBAC : PLAYER/REFEREE/AUTHOR/ADMIN)

* **Jakarta Validation** (DTO REST)

* **Data** : PostgreSQL (JDBC ou R2DBC), **Redis** (timers/locks/pub-sub), **S3/MinIO** (médias)

* **Moteur d’états** : **Spring Statemachine** (ou machine réactive custom)

* **Observabilité** : Micrometer \+ OpenTelemetry (traces), Prometheus/Grafana, logs JSON (ELK/Loki), Sentry

* **Tests** : JUnit 5, AssertJ, **Testcontainers** (Postgres/Redis/MinIO), WireMock

* **Qualité** : Gradle, Checkstyle, SpotBugs, JaCoCo

### **6.3 Moteur « plugins » (modules/engine)**

`public interface RulePlugin {`  
  `RoundType type();`  
  `RoundState init(MatchContext ctx);                 // entrée dans la rubrique`  
  `RoundState onTick(MatchContext ctx, Duration dt);  // tick horloge serveur`  
  `RoundState onAnswer(MatchContext ctx, AnswerPayload payload);`  
  `void applyScoring(MatchContext ctx);               // points, bonus, pénalités`  
`}`

**Invariants codés (exemples)**

* **SMASH** : **3 s** pour annoncer la question, sinon **\+10** à l’adverse.

* **RELAIS** : **sans faute** et **≤ 40 s** ⇒ **\+20**.

* **ESTOCADE** : **40 pts** par question.

* **CIME** : **3 jokers**, paliers **\+20** (\>4), **\+40** (\>7).

* **TIRS AU BUT** : victoire de séance **\+40**.

* **IDENTIFICATION** : indices **40/30/20/10**.

* **SPRINT FINAL** : **20 éclairs**.

### **6.4 Modèle de données (PostgreSQL, abrégé)**

* `user(id, handle, email, role, rating, country)`

* `team(id, name, captain_user_id)`

* `match(id, mode, status, rules_version, region, created_at)`

* `round(id, match_id, type, state_json, started_at, timer_ms, theme_id)`

* `question(id, rubric, theme, locale, difficulty, format, statement, choices_json, correct, hints_json, points_default, source_ref)`

* `round_question(round_id, question_id, player_id, team_id, answer_given, is_correct, points_awarded, time_ms)`

* `penalty(id, match_id, user_id, reason, count)` → **suspension à 5**

* `auction(id, round_id, team_id, bid_points, hint_level)` (JACKPOT)

* `joker_use(round_id, helper_user_id, target_user_id)` (CIME)

* `media(id, type, url, rights)` (audio/images)

**Index** : `(rubric, theme, locale, difficulty)`, clés étrangères usuelles.  
 **Invariants** : applicatifs (engine) \+ contraintes métiers si pertinent (ex. points \= 40 pour ESTOCADE).

### **6.5 API REST (principales)**

* `POST /matches` — créer un match

* `POST /matches/{id}/start` — démarrer (arbitre)

* `POST /matches/{id}/rounds/next` — aller à la rubrique suivante

* `POST /rounds/{id}/answer` — soumettre une réponse (throttle)

* `POST /rounds/{id}/penalty` — appliquer une pénalité

* `GET /matches/{id}/state` — état (scores, chrono, rubrique, contrôles)

**Sécurité** : JWT (Bearer), RBAC (guards arbitre/admin/auteur), CORS strict.

### **6.6 WebSocket / STOMP (modules/realtime)**

* **Endpoint** : `/ws`

* **Topics (rooms)** :

  * `/topic/match.{id}` — état global

  * `/topic/team.A`, `/topic/team.B` — infos dédiées d’équipe

  * `/topic/referee.{id}` — canal arbitre

* **Événements** :

  * `state.update` — changement d’état / chrono (tick 100–250 ms)

  * `question.show` — énoncé \+ média/indice

  * `answer.result` — verdict \+ delta points

  * `auction.update` — JACKPOT

  * `theme.selected` — PANIER/RELAIS/CIME

  * `penalty.applied` — cumul pénalités / suspension

### **6.6.1 Événements WebSocket SMASH (détail)**

Le workflow SMASH utilise les événements suivants :

| Événement | Direction | Description |
|-----------|-----------|-------------|
| `SMASH_TURN_START` | Server → Client | Début du tour (turnNumber, attackerTeam, defenderTeam, roundType) |
| `SMASH_CONCERTATION` | Server → Client | Phase concertation démarrée (SMASH A uniquement) |
| `SMASH_TOP` | Client → Server | Bouton TOP pressé par l'attaquant |
| `SMASH_TOP` | Server → Client | TOP confirmé, démarre le chrono 3s |
| `SMASH_QUESTION_SUBMIT` | Client → Server | Attaquant soumet la question |
| `SMASH_QUESTION_SUBMIT` | Server → Client | Question reçue, passage en validation |
| `SMASH_VALIDATE_PROMPT` | Server → Client | Demande au défenseur de valider |
| `SMASH_QUESTION_VALID` | Server → Client | Question validée, passage en réponse |
| `SMASH_QUESTION_INVALID` | Server → Client | Question invalidée (+10 défenseur) |
| `SMASH_ANSWER_PROMPT` | Server → Client | Demande au défenseur de répondre (10s) |
| `SMASH_ANSWER_SUBMIT` | Client → Server | Défenseur soumet sa réponse |
| `SMASH_ANSWER_SUBMIT` | Server → Client | Réponse reçue, passage en résultat |
| `SMASH_RESULT_PROMPT` | Server → Client | Demande à l'attaquant de valider la réponse |
| `SMASH_ANSWER_CORRECT` | Server → Client | Réponse validée correcte (+10 défenseur) |
| `SMASH_ANSWER_INCORRECT` | Server → Client | Réponse incorrecte (0 pts) |
| `SMASH_TIMEOUT` | Server → Client | Timeout sur une phase (question, validation, réponse) |
| `SCORE_UPDATED` | Server → Client | Mise à jour des scores en temps réel |
| `ROUND_ENDED` | Server → Client | Fin de la manche SMASH |

**Endpoints WebSocket SMASH :**

* `/app/match/{matchId}/smash/top` — Envoyer le TOP
* `/app/match/{matchId}/smash/question` — Soumettre une question
* `/app/match/{matchId}/smash/validate` — Valider/invalider la question
* `/app/match/{matchId}/smash/answer` — Soumettre une réponse
* `/app/match/{matchId}/smash/result` — Valider le résultat (correct/incorrect)

### **6.7 Anti-triche**

* **Server authoritative** (horloge/états), **idempotence** commandes.

* **Rate-limit/Throttling** (Bucket4j/Redis) : 1 soumission/joueur/question.

* **Détection** : réponses trop rapides, multi-session, copier/coller.

* **Journal append-only** : hash chaîné (preuve litige).

* **Classé** : mode **arbitre** requis (option voix/vidéo).

### **6.8 Observabilité & SRE**

* **Micrometer** → **Prometheus/Grafana** (latence WS, erreurs, drift chrono).

* **OpenTelemetry** (traces) → Jaeger/Tempo.

* **Logs JSON** → ELK/Loki.

* **Feature flags** : Unleash/ConfigCat (activer rubriques).

### **6.9 Configuration & exécution (dev)**

* `infra/docker-compose.yml` (Postgres : `5432`, Redis : `6379`, MinIO : `9000`)

`.env` (exemple) :

 `SPRING_PROFILES_ACTIVE=dev`  
`DB_URL=jdbc:postgresql://localhost:5432/mindsoccer`  
`DB_USER=ms_user`  
`DB_PASSWORD=ms_pass`  
`REDIS_URL=redis://localhost:6379`  
`S3_ENDPOINT=http://localhost:9000`  
`S3_BUCKET=mindsoccer`  
`JWT_SECRET=change-me`

*   
* **Lancer** : `docker compose up -d` puis `./gradlew :apps:api:bootRun`

---

## **7\) `mindsoccer-frontend` — Monorepo (Next.js \+ React Native)**

### **7.1 Arborescence (pnpm \+ Turbo/Nx)**

`mindsoccer-frontend/`  
`├─ package.json`  
`├─ pnpm-workspace.yaml`  
`├─ turbo.json`  
`├─ apps/`  
`│  ├─ web/                 # Next.js (React)`  
`│  └─ mobile/              # React Native (Expo)`  
`├─ packages/`  
`│  ├─ ui/                  # Design system (RN Web + RN Paper/Tamagui)`  
`│  ├─ api-client/          # SDK REST/WS généré depuis OpenAPI du back`  
`│  ├─ protocol-ts/         # Types TS (DTO, enums) alignés avec le back`  
`│  └─ utils/               # i18n, time, formatage, hooks partagés`  
`└─ .env.example`

### **7.2 Web (Next.js)**

* **Pages** : `/` (landing/SEO), `/rules`, `/lobby`, `/match/{id}`, `/referee/{id}`, `/studio`, `/admin`.

* **SEO** pour communauté/partages, **spectateur** lecture seule, **studio d’auteurs** (import CSV/Docs).

### **7.3 Mobile (React Native / Expo)**

* **Écrans** : Onboarding → Lobby → Match (UI chrono & actions) → Score → Historique.

* **OTA updates**, notifications, **React Native Web** pour partager les composants avec le web (boutons, chronos, modales).

### **7.4 Client API (packages/api-client)**

* Généré depuis l’**OpenAPI** exposé par `mindsoccer-backend` (tâche CI).

* **WS** : wrapper STOMP/WebSocket pour évènements (`state.update`, `question.show`, etc.).

* **Auth** : stockage JWT (secure storage / cookies HTTPOnly sur web).

### **7.5 Config & exécution (dev)**

`.env.local` (exemple) :

 `NEXT_PUBLIC_API_BASE_URL=http://localhost:8080`  
`NEXT_PUBLIC_WS_URL=ws://localhost:8080/ws`

*   
* **Lancer web** : `pnpm -w install && pnpm --filter web dev`

* **Lancer mobile** : `pnpm --filter mobile start` (Expo)

---

## **8\) Contrats & synchronisation Back ↔ Front**

* **Spécification OpenAPI** versionnée dans `mindsoccer-backend/modules/protocol`.

* CI du back : **publie** le bundle OpenAPI (artefact GitHub/registry).

* CI du front : **récupère** la spec → **génère** `packages/api-client` (OpenAPI Generator) \+ met à jour `packages/protocol-ts` (types TS alignés).

* **Versionnage** semver (ex. `0.3.0`) pour détecter les breaks.

---

## **9\) Sécurité (front \+ back)**

* **JWT \+ refresh** (Spring Security 6\) ; RBAC (PLAYER/REFEREE/AUTHOR/ADMIN).

* **CORS** strict (origines front).

* **Rate-limit** (Bucket4j) côté back ; **throttle** actions côté front.

* **Validation** et **sanitization** (anti-XSS) sur textes libres (studio).

* **Droits médias** (S3/MinIO) : URL signées si nécessaire.

---

## **10\) Observabilité & SRE (fin à fin)**

* Métriques front : Web Vitals \+ erreurs UI (Sentry).

* Métriques back : latence WS/REST, erreurs plugin, dérive horloge.

* Traces distribuées (OTel) si un jour microservices.

* Dashboards Grafana : **Match loop** (SMASH→…→SPRINT), délais moyens de réponse, taux réussite par rubrique, pénalités/suspensions.

---

## **11\) Tests & qualité**

* **Back** :

  * Unitaires (plugins de règles, invariants),

  * Intégration (REST/WS) avec Testcontainers (Postgres, Redis, MinIO),

  * Contrats OpenAPI (Springdoc tests),

  * Couverture JaCoCo (seuils : 80 % sur engine/scoring).

* **Front** :

  * Unitaires (React Testing Library),

  * E2E (Playwright/Detox),

  * Tests visuels (Chromatic) pour le design system.

---

## **12\) CI/CD & environnements**

* **Branches** : `main` (prod), `develop` (préprod), features `feat/*`.

* **Pipelines (GitHub Actions)** :

  * Back : build, tests, publish OpenAPI \+ image Docker, déploiement Helm/K8s.

  * Front : lint/build/test, génération SDK depuis OpenAPI, déploiement (Vercel/Static host) \+ Expo EAS pour mobile.

* **Envs** : `dev` (Docker compose), `staging` (préprod), `prod` (K8s).

* **Stratégies** : blue/green ou canary.

---

## **13\) Roadmap de réalisation**

### **MVP (Gameplay prioritaire)**

1. Flux : Lobby → **SMASH → CASCADE → PANIER → RELAIS → ESTOCADE → SPRINT FINAL**.

2. Banque initiale : import CSV/Excel \+ tags rubrique/thème/difficulté \+ sources/indices.

3. Parties privées (host arbitre), **pénalités & suspensions** conformes.

4. Web & RN : mêmes contrats WS, chrono synchro serveur, design system partagé.

### **V1+**

* **JACKPOT**, **CIME** (échelle \+ 3 jokers, paliers 20/40), **TIRS AU BUT** (+40), **RANDONNÉE LEXICALE**, **ÉCHAPPÉE**, **CAPOEIRA**.

* Matchmaking public (ELO), classements, tournois, packs **FOCUS**, replays/spectateur enrichi.

---

## **14\) Checklist d’acceptation (qualité)**

* **Conformité règles** :

  * SMASH : annonce **≤ 3 s**, sinon **\+10** adverse.

  * RELAIS : **≤ 40 s** sans faute \= **\+20**.

  * ESTOCADE : **3× 40 pts**.

  * CIME : **3 jokers**, paliers **\+20** (\>4) & **\+40** (\>7).

  * TIRS AU BUT : victoire séance **\+40**.

  * IDENTIFICATION : **40/30/20/10**.

  * SPRINT FINAL : **20** éclairs.

* **Pénalités** → question bonus ; **5** ⇒ **suspension** (4×10 ou 1×40).

* **Horloge serveur** (tick 100–250 ms) & **rooms** WS, **idempotence** \+ **locks Redis**.

* **Studio d’auteurs** : tags, indices, sources, droits médias.

* **Observabilité** : métriques WS, traces, logs, alertes SLO.

---

## **15\) Extraits code (illustratifs)**

### **15.1 Back — STOMP config**

`@EnableWebSocketMessageBroker`  
`public class WsConfig implements WebSocketMessageBrokerConfigurer {`  
  `@Override public void configureMessageBroker(MessageBrokerRegistry config) {`  
    `config.enableSimpleBroker("/topic");`  
    `config.setApplicationDestinationPrefixes("/app");`  
  `}`  
  `@Override public void registerStompEndpoints(StompEndpointRegistry registry) {`  
    `registry.addEndpoint("/ws").setAllowedOriginPatterns("*");`  
  `}`  
`}`

### **15.2 Back — invariant Estocade (40 pts)**

`if (round.type() == RoundType.ESTOCADE && answer.isFinalized()) {`  
  `scoringService.addPoints(teamId, answer.isCorrect() ? 40 : 0);`  
`}`

### **15.3 Engine — interface plugin**

`public interface RulePlugin {`  
  `RoundType type();`  
  `RoundState init(MatchContext ctx);`  
  `RoundState onTick(MatchContext ctx, Duration dt);`  
  `RoundState onAnswer(MatchContext ctx, AnswerPayload payload);`  
  `void applyScoring(MatchContext ctx);`  
`}`

