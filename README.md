# Villa Vredestein — Backend

REST API voor het beheersysteem van Villa Vredestein, een studentenpension. Gebouwd met Spring Boot 3 en Java 21.

---

## Technische stack

| Onderdeel | Keuze |
|-----------|-------|
| Framework | Spring Boot 3.3.4 |
| Taal | Java 21 |
| Database | PostgreSQL |
| Authenticatie | JWT (JJWT) |
| Wachtwoord hashing | BCrypt |
| Mail | Gmail SMTP via JavaMailSender |
| Tests | JUnit 5, Mockito, Testcontainers |
| Coverage | JaCoCo |
| Build | Maven |

---

## Rollen

| Rol | Omschrijving |
|-----|--------------|
| `ADMIN` | Beheert gebruikers, facturen, documenten en schoonmaaktaken |
| `STUDENT` | Ziet eigen facturen, betalingen, taken en documenten |
| `CLEANER` | Beheert schoonmaaktaken en incidenten |

---

## API endpoints (overzicht)

### Auth
| Method | Endpoint | Toegang |
|--------|----------|---------|
| POST | `/api/auth/login` | Publiek |
| POST | `/api/auth/password/forgot` | Publiek |
| POST | `/api/auth/password/reset` | Publiek |

### Gebruikers
| Method | Endpoint | Toegang |
|--------|----------|---------|
| GET | `/api/users/me` | Alle rollen |
| PUT | `/api/users/me/profile` | Alle rollen |
| PATCH | `/api/users/me/password` | Alle rollen |
| POST | `/api/users/me/profile-photo` | Alle rollen |
| GET | `/api/users` | ADMIN |
| POST | `/api/users` | ADMIN |
| PUT/DELETE | `/api/users/{id}` | ADMIN |

### Facturen
| Method | Endpoint | Toegang |
|--------|----------|---------|
| GET | `/api/invoices` | ADMIN |
| GET | `/api/invoices/me` | STUDENT |
| GET | `/api/invoices/{id}` | STUDENT (eigen) / ADMIN |
| POST | `/api/invoices` | ADMIN |
| PUT | `/api/invoices/{id}/status` | ADMIN |
| DELETE | `/api/invoices/{id}` | ADMIN |

### Betalingen
| Method | Endpoint | Toegang |
|--------|----------|---------|
| GET | `/api/payments/me` | STUDENT |
| GET | `/api/payments/me/open` | STUDENT |
| GET | `/api/payments` | ADMIN |
| POST | `/api/payments` | ADMIN |
| DELETE | `/api/payments/{id}` | ADMIN |

### Documenten
| Method | Endpoint | Toegang |
|--------|----------|---------|
| GET | `/api/documents` | Alle rollen |
| POST | `/api/documents/upload` | ADMIN |
| GET | `/api/documents/{id}/download` | Alle rollen (op basis van roleAccess) |
| DELETE | `/api/documents/{id}` | ADMIN |

### Schoonmaaktaken
| Method | Endpoint | Toegang |
|--------|----------|---------|
| GET | `/api/cleaning/tasks` | Alle rollen |
| GET | `/api/cleaning/tasks/me` | Alle rollen |
| POST | `/api/cleaning/tasks` | ADMIN / CLEANER |
| PUT | `/api/cleaning/tasks/{id}/toggle` | Alle rollen |
| PUT | `/api/cleaning/tasks/{id}/comment` | ADMIN / CLEANER |
| PUT | `/api/cleaning/tasks/{id}/incident` | ADMIN / CLEANER |
| DELETE | `/api/cleaning/tasks/{id}` | ADMIN |

### Admin job triggers
| Method | Endpoint | Omschrijving |
|--------|----------|--------------|
| POST | `/api/admin/jobs/reminders/trigger` | Factuurherinneringen handmatig starten |
| POST | `/api/admin/jobs/overdue/trigger` | Vervallen facturen handmatig starten |
| POST | `/api/admin/jobs/cleaning/missed/trigger` | Gemiste taken handmatig starten |

---

## Geplande taken (Scheduled Jobs)

| Job | Tijdstip | Omschrijving |
|-----|----------|--------------|
| `InvoiceReminderJob` | Dagelijks 09:00 | Herinnering voor facturen die binnenkort vervallen |
| `OverdueInvoiceJob` | Dagelijks 09:15 | Mail bij achterstallige facturen |
| `MissedCleaningTaskJob` | Dagelijks 09:30 | Mail bij niet-voltooide schoonmaaktaken na deadline |

---

## Lokaal draaien

### Vereisten
- Java 21
- Maven
- PostgreSQL (of Docker)

### 1. Database aanmaken

```sql
CREATE DATABASE villavredestein;
```

### 2. `.env` bestand aanmaken

Maak een `.env` bestand aan in de root van het project (zie `.env.example` voor alle variabelen):

```env
SPRING_PROFILES_ACTIVE=dev

DB_URL=jdbc:postgresql://localhost:5432/villavredestein
DB_USERNAME=<jouw-db-gebruiker>
DB_PASSWORD=<jouw-db-wachtwoord>

JWT_SECRET=<willekeurige-string-van-minimaal-32-tekens>
JWT_EXPIRY_SECONDS=86400

APP_CORS_ALLOWED_ORIGINS=http://localhost:5173

SEED_ENABLED=true
SEED_ADMIN_EMAIL=<admin-email>
SEED_ADMIN_PASSWORD=<admin-wachtwoord>
SEED_CLEANER_EMAIL=<cleaner-email>
SEED_CLEANER_PASSWORD=<cleaner-wachtwoord>
SEED_STUDENT_EMAILS=<email1>,<email2>
SEED_STUDENT_PASSWORD=<student-wachtwoord>

MAIL_ENABLED=false
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=<gmail-adres>
MAIL_PASSWORD=<gmail-app-wachtwoord>
MAIL_FROM=<gmail-adres>
MAIL_BCC_ADMIN=<gmail-adres>
```

> **Nooit echte wachtwoorden in de README of in git plaatsen.** Het `.env` bestand staat in `.gitignore`.

> **Let op:** gebruik een Gmail App Password, niet je normale Gmail wachtwoord. Activeer dit via Google Account → Beveiliging → App-wachtwoorden.

### 3. Applicatie starten

```bash
mvn spring-boot:run
```

De API is bereikbaar op `http://localhost:8080`.

---

## Tests uitvoeren

```bash
# Alleen unit tests
mvn test

# Unit tests + integratietests (vereist Docker voor Testcontainers)
mvn verify
```

De integratietests draaien met een echte PostgreSQL-instantie via Testcontainers. Zorg dat Docker actief is.

### Codecoverage

```bash
mvn verify
open target/site/jacoco/index.html
```

`InvoiceService` en `MailService` hebben 100% line coverage.

---

## Omgevingsvariabelen (volledig overzicht)

| Variabele | Omschrijving | Standaardwaarde |
|-----------|--------------|-----------------|
| `SPRING_PROFILES_ACTIVE` | Actief profiel (`dev` of `prod`) | `prod` |
| `DB_URL` | JDBC-verbindingsstring | — |
| `DB_USERNAME` | Databasegebruiker | — |
| `DB_PASSWORD` | Databasewachtwoord | — |
| `JPA_DDL_AUTO` | Hibernate DDL strategie | `update` |
| `JWT_SECRET` | Geheime sleutel voor JWT (min. 32 bytes) | — |
| `JWT_EXPIRY_SECONDS` | Geldigheidsduur token in seconden | `3600` |
| `APP_CORS_ALLOWED_ORIGINS` | Toegestane CORS origins | `http://localhost:5173` |
| `APP_UPLOAD_DIR` | Map voor geüploade bestanden | `uploads` |
| `SEED_ENABLED` | Testgebruikers aanmaken bij opstarten | `false` |
| `MAIL_ENABLED` | E-mail versturen inschakelen | `true` |
| `MAIL_HOST` | SMTP host | `smtp.gmail.com` |
| `MAIL_PORT` | SMTP poort | `587` |
| `MAIL_USERNAME` | SMTP gebruikersnaam | — |
| `MAIL_PASSWORD` | SMTP wachtwoord (App Password) | — |
| `MAIL_FROM` | Afzenderadres | — |
| `MAIL_BCC_ADMIN` | BCC-adres voor alle uitgaande mails | — |

---


## Projectstructuur

```
src/
├── main/java/com/villavredestein/
│   ├── config/          # SecurityConfig, DevSeeder
│   ├── controller/      # REST controllers
│   ├── dto/             # Request- en response-objecten
│   ├── jobs/            # Geplande taken (mail)
│   ├── model/           # JPA entiteiten
│   ├── repository/      # Spring Data JPA repositories
│   ├── security/        # JWT filter en service
│   └── service/         # Bedrijfslogica
└── test/java/com/villavredestein/
    ├── integration/     # Integratietests (Testcontainers)
    └── service/         # Unit tests
```

---

## Beveiliging

- Alle endpoints behalve `/api/auth/**` en `/actuator/health` vereisen een geldig JWT token
- Wachtwoorden worden opgeslagen als BCrypt hash
- Uploads worden gevalideerd op padtraversal
- CORS is geconfigureerd via omgevingsvariabelen
- `.env` staat in `.gitignore` en wordt nooit gecommit