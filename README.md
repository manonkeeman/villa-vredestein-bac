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
7. Rollen & testgebruikers  
8. API‑gebruik  
9. Teststrategie  
10. Auteur

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
http://localhost:8432
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
export DB_URL=jdbc:postgresql://localhost:5432/villa_vredestein_test
export DB_USERNAME=postgres
export DB_PASSWORD=<POSTGRES_PASSWORD>
```

In het testprofiel wordt het schema automatisch aangemaakt en verwijderd:

```yaml
spring.jpa.hibernate.ddl-auto=create-drop
```

---

## 7. Rollen & testgebruikers

Omdat gebruikers niet via de API aangemaakt kunnen worden, zijn vaste testaccounts aanwezig.

### ADMIN
```
email: admin@villavredestein.nl
password: ADMIN_PASSWORD
```

### CLEANER
```
email: cleaner@villavredestein.nl
password: CLEANER_PASSWORD
```

### STUDENTEN
```
email: student1@villavredestein.nl
password: STUDENT_PASSWORD
```

```
email: student2@villavredestein.nl
password: STUDENT_PASSWORD
```

---

## 8. API‑gebruik

### Inloggen (JWT ophalen)

```http
POST /api/auth/login
Content-Type: application/json

{
  "email": "admin@villavredestein.nl",
  "password": "ADMIN_PASSWORD"
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

## 9. Teststrategie

### Unit tests
- Gericht op service‑laag
- Arrange – Act – Assert structuur
- 100% line coverage op geselecteerde services

Voorbeelden:
- `InvoiceServiceTest`
- `MailServiceTest`

### Integratietests
- `@SpringBootTest` + `MockMvc`
- PostgreSQL database
- Inclusief security en autorisatie

Voorbeelden:
- `AuthIntegrationTest`
- `InvoiceIntegrationTest`

Tests uitvoeren:

```bash
mvn clean test
```

---
## 10. Auteur

**Manon Keeman**  
https://www.manonkeeman.com
