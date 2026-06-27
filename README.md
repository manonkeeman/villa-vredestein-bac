# Villa Vredestein — Backend API

REST API voor het beheersysteem van Villa Vredestein, een studentenpension.  
Gebouwd met **Spring Boot 3** en **Java 21**.

De volledige API-documentatie (alle endpoints, request bodies en voorbeeldresponses) is beschikbaar als **Postman collection** in de map `docs/`.

---

## Technische stack

| Onderdeel          | Keuze                            |
|--------------------|----------------------------------|
| Framework          | Spring Boot 3.3.4                |
| Taal               | Java 21                          |
| Database           | PostgreSQL 16                    |
| Authenticatie      | JWT (JJWT) — stateless           |
| Wachtwoord hashing | BCrypt (sterkte 8)               |
| Betalen            | Mollie iDEAL                     |
| Mail               | Gmail SMTP via JavaMailSender    |
| Tests              | JUnit 5, Mockito, Testcontainers |
| Coverage           | JaCoCo                           |
| Build              | Maven                            |

---

## Rollen

| Rol       | Omschrijving                                                    |
|-----------|-----------------------------------------------------------------|
| `ADMIN`   | Beheert gebruikers, kamers, facturen, documenten en taken      |
| `STUDENT` | Ziet eigen facturen, betalingen, taken en documenten           |
| `CLEANER` | Beheert schoonmaaktaken en rapporteert incidenten              |

Alle endpoints behalve `/api/auth/**` en `/actuator/health` vereisen een geldig **Bearer JWT** in de `Authorization`-header.

---

## Lokaal draaien

### Vereisten

- Java 21
- Maven 3.9+
- PostgreSQL 15+ (of Docker)
- Docker (alleen voor integratietests via Testcontainers)

### 1. Database aanmaken

```sql
CREATE DATABASE villavredestein;
```

### 2. `.env` aanmaken

Maak een `.env` bestand in de projectroot. Zie `.env.example` voor alle variabelen:

```env
SPRING_PROFILES_ACTIVE=dev

DB_URL=jdbc:postgresql://localhost:5432/villavredestein
DB_USERNAME=<jouw-db-gebruiker>
DB_PASSWORD=<jouw-db-wachtwoord>

JWT_SECRET=<willekeurige-string-van-minimaal-32-tekens>
JWT_EXPIRY_SECONDS=3600

APP_CORS_ALLOWED_ORIGINS=http://localhost:5173
APP_UPLOAD_DIR=uploads
FRONTEND_URL=http://localhost:5173
RENT_AMOUNT=350.00

SEED_ENABLED=false
SEED_ADMIN_EMAIL=<admin-e-mailadres>
SEED_ADMIN_PASSWORD=<admin-wachtwoord>
SEED_CLEANER_EMAIL=<cleaner-e-mailadres>
SEED_CLEANER_PASSWORD=<cleaner-wachtwoord>
SEED_STUDENT_EMAILS=<email1>,<email2>
SEED_STUDENT_PASSWORD=<student-wachtwoord>

MAIL_ENABLED=false
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=<gmail-adres>
MAIL_PASSWORD=<gmail-app-wachtwoord>
MAIL_FROM=<afzenderadres>
MAIL_BCC_ADMIN=<bcc-adres>

MOLLIE_API_KEY=<mollie-api-sleutel>
MOLLIE_WEBHOOK_URL=<publieke-webhook-url>
```

> Het `.env` bestand staat in `.gitignore`. Zet nooit wachtwoorden of sleutels in versiebeheer.

### 3. Opstarten

```bash
mvn spring-boot:run
```

De API is bereikbaar op `http://localhost:8080`.

---

## Tests uitvoeren

### Unit tests (geen Docker nodig)

```bash
mvn test
```

### Integratietests (Docker vereist)

De integratietests gebruiken **Testcontainers**: er wordt automatisch een tijdelijke PostgreSQL Docker-container gestart en na afloop opgeruimd. Er wordt geen `@MockBean` gebruikt — alle lagen worden echt aangestuurd.

**Vereiste:** Docker Desktop moet draaien voordat je deze tests uitvoert.

```bash
# Start Docker Desktop, daarna:
mvn verify
```

Of via het Maven-profiel:

```bash
mvn verify -PrunIT
```

Controleer of Docker actief is met:

```bash
docker info
```

### Codecoverage

```bash
mvn verify
open target/site/jacoco/index.html
```

Gedekte services met unit tests: `InvoiceService`, `MailService`, `CleaningTaskService`, `CleaningScheduleService`, `RoomService`, `PaymentService`.

---

## Projectstructuur

```
src/
├── main/java/com/villavredestein/
│   ├── config/       # SecurityConfig, GlobalExceptionHandler, seeders
│   ├── controller/   # REST-controllers
│   ├── dto/          # Request- en response-DTOs (met Bean Validation)
│   ├── jobs/         # Geplande taken (@Scheduled)
│   ├── model/        # JPA-entiteiten
│   ├── repository/   # Spring Data JPA repositories
│   ├── security/     # JwtService, JwtAuthenticationFilter
│   └── service/      # Bedrijfslogica
└── test/java/com/villavredestein/
    ├── integration/  # Integratietests (Testcontainers + MockMvc)
    └── service/      # Unit tests (Mockito)
```

---

## Beveiliging

- **JWT** — stateless authenticatie; elk verzoek valideert het token in `JwtAuthenticationFilter`
- **Ownership-check** — studenten kunnen uitsluitend hun eigen facturen, PDF's en betalingen opvragen; dit wordt gecontroleerd in de service-laag, niet alleen op rolniveau
- **Invoervalidatie** — Bean Validation (`@Valid`, `@NotBlank`, `@Email`, `@Size` e.d.) op alle request-DTOs; de `GlobalExceptionHandler` mapt validatiefouten naar HTTP 400
- **BCrypt** — wachtwoorden worden nooit als plain-text opgeslagen
- **CORS** — geconfigureerd via omgevingsvariabelen; geen wildcard in productie
- **Uploads** — validatie op bestandstype en padtraversal; maximale bestandsgrootte 5 MB
- **Geheimen** — alle sleutels en wachtwoorden via `.env`; nooit hardcoded in de codebase
