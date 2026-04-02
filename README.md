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

## Functionaliteiten

### 1. Authenticatie
- Inloggen met e-mail en wachtwoord via `POST /api/auth/login`
- JWT token als response, verplicht meegestuurd als `Authorization: Bearer <token>`
- Studenten moeten bij inloggen hun kamernaam bevestigen
- Wachtwoord vergeten / reset via e-mail token

### 2. Gebruikersbeheer
- Admin kan gebruikers aanmaken, inzien, bewerken en verwijderen
- Iedere gebruiker kan zijn eigen profiel bijwerken en profielfoto uploaden
- Wachtwoord wijzigen via `/api/users/me/password`

### 3. Kamers
- Overzicht van alle kamers en wie erin woont
- Admin koppelt kamers aan studenten

### 4. Documenten
- Admin upload documenten (PDF, etc.) met rolgebaseerde toegang (`ALL`, `STUDENT`, `CLEANER`, `ADMIN`)
- Studenten ontvangen automatisch een e-mailnotificatie bij een nieuw document
- Downloaden via `GET /api/documents/{id}/download` — PDFs openen inline in de browser

### 5. Facturen & Betalingen
- Admin maakt facturen aan per student per maand
- Statussen: `OPEN`, `PAID`, `OVERDUE`, `CANCELLED`
- Studenten zien eigen facturen via `GET /api/invoices/me`
- Betalingsgeschiedenis via `GET /api/payments/me`
- Automatische herinneringsmail X dagen voor vervaldatum (standaard 4 dagen)
- Automatische vervallen-mail na de vervaldatum

### 6. Schoonmaaktaken
- Wekelijkse taakverdeling (week 1–4 rotatie)
- Taken hebben een deadline, naam, beschrijving en toegewezen bewoner
- Student ziet eigen taken via `GET /api/cleaning/tasks/me`
- Admin en cleaner zien alles en kunnen commentaar en incidenten toevoegen
- Automatische e-mailnotificatie bij niet-voltooide taken na de deadline

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
DB_USERNAME=postgres
DB_PASSWORD=postgres

JWT_SECRET=<minimaal-32-tekens-willekeurige-string>
JWT_EXPIRY_SECONDS=86400

APP_CORS_ALLOWED_ORIGINS=http://localhost:5173

SEED_ENABLED=true
SEED_ADMIN_EMAIL=admin@voorbeeld.com
SEED_ADMIN_PASSWORD=AdminWachtwoord1!
SEED_CLEANER_EMAIL=cleaner@voorbeeld.com
SEED_CLEANER_PASSWORD=CleanerWachtwoord1!
SEED_STUDENT_EMAILS=student1@voorbeeld.com,student2@voorbeeld.com
SEED_STUDENT_PASSWORD=StudentWachtwoord1!

MAIL_ENABLED=false
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=
MAIL_PASSWORD=
MAIL_FROM=
MAIL_BCC_ADMIN=
```

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