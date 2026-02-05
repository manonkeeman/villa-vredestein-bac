# Villa Vredestein – Backend Web API

Een professionele Spring Boot Web‑API voor het beheren van studentenhuis Villa Vredestein.  
De applicatie is gebouwd volgens REST‑principes en maakt gebruik van JWT‑authenticatie** met rol‑gebaseerde autorisatie.

---

## Inhoudsopgave

1. Projectoverzicht  
2. Functionaliteiten  
3. Architectuur  
4. Technische stack  
5. Installatie & configuratie  
6. Database  
7. API‑gebruik
8. Teststrategie
9. Auteur

---

## 1. Projectoverzicht

Villa Vredestein is een backend applicatie voor het beheren van:
- studenten
- facturen en betalingen
- schoonmaaktaken
- documenten
- notificaties

Architectuur:

```
Controller → Service → Repository → PostgreSQL
```

---

## 2. Functionaliteiten

- JWT‑authenticatie
- Rol‑gebaseerde autorisatie (ADMIN, STUDENT, CLEANER)
- Gebruikersbeheer
- Facturen & betalingen
- Schoonmaaktaken
- Document upload & download
- E‑mailnotificaties
- Scheduled jobs (factuurherinneringen)

---

## 3. Architectuur

De applicatie is opgebouwd volgens een gelaagde architectuur:

```
com.villavredestein
├── config        # Security, JWT, Mail
├── controller    # REST endpoints
├── dto           # Request/Response DTO’s
├── jobs          # Scheduled jobs
├── model         # JPA entiteiten
├── repository    # JPA repositories
├── security      # JWT filter & UserDetailsService
├── service       # Business logica
```

---

## 4. Technische stack

| Technologie           | Gebruik                     |
|-----------------------|-----------------------------|
| Java 17               | Programmeertaal             |
| Spring Boot 3         | Web framework               |
| Spring Security + JWT | Authenticatie & autorisatie |
| PostgreSQL            | Relationele database        |
| JPA / Hibernate       | ORM                         |
| Maven                 | Build tool                  |
| JUnit 5               | Unit & integratietests      |
| Mockito               | Unit testing                |
| MockMvc               | Integratietests             |

---

## 5. Installatie & configuratie

### Vereisten

- Java 17
- Maven 3.8+
- PostgreSQL
- Git

### Project clonen

```bash
git clone https://github.com/manonkeeman/villa-vredestein-bac.git
cd villa-vredestein-bac
```

### Applicatie starten

```bash
mvn clean spring-boot:run
```

De API draait op:

```
http://localhost:8080
```

---

## 6. Database

De applicatie gebruikt PostgreSQL, ook voor integratietests.

Maak lokaal een testdatabase aan:

```sql
CREATE DATABASE villa_vredestein_test;
```

Configureer environment variables:

```bash
export DB_URL=jdbc:postgresql://localhost:8080/villa_vredestein_test
export DB_USERNAME=postgres
export DB_PASSWORD=<POSTGRES_PASSWORD>
```

In het testprofiel wordt het schema automatisch aangemaakt en verwijderd:

```yaml
spring.jpa.hibernate.ddl-auto=create-drop
```

---


## 7. API‑gebruik

### Inloggen 

```http
POST /api/auth/login
Content-Type: application/json

{
  "email": "admin@villavredestein.com",
  "password": "<admin-password-via-environment-variable>"
}
```

Response:
```json
{
  "token": "<jwt-token>"
}
```

Gebruik dit token in vervolgrequests:

```
Authorization: Bearer <jwt-token>
```

---

## 8. Teststrategie

### Unit tests
- Gericht op de service-laag
- Arrange – Act – Assert structuur
- Geen afhankelijkheid van externe systemen of secrets

Voorbeelden:
- `InvoiceServiceTest`
- `MailServiceTest`

### Integratietests & security

De applicatie bevat integratietests die gebruikmaken van beveiligde gebruikers (zoals ADMIN) en JWT-authenticatie.

Om te voorkomen dat wachtwoorden of andere gevoelige gegevens in de codebase, testbestanden of Git-repository terechtkomen,
wordt alle secret-configuratie uitsluitend via environment variables aangeleverd.

#### Vereiste environment variables

Voor het uitvoeren van alle integratietests is de volgende environment variable vereist:

- `SEED_ADMIN_PASSWORD`

Deze variabele wordt gebruikt om tijdens integratietests een ADMIN-gebruiker te seeden met een versleuteld wachtwoord.


#### Optioneel: alle integratietests uitvoeren

Indien gewenst kan een examinator lokaal alle integratietests uitvoeren door vooraf één environment variable te zetten:

```bash
export SEED_ADMIN_PASSWORD=local_test_only_password
```

Daarna kunnen alle tests worden uitgevoerd met:

```bash
mvn clean test
```


## 9. Auteur

**Manon Keeman**  
https://www.manonkeeman.com
