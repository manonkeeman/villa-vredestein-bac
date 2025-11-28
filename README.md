# Villa Vredestein – Backend 

De backend van de Villa Vredestein applicatie vormt het hart van een full‑stack beheersysteem voor een studentenhuis.  
De API is gebouwd met **Spring Boot 3**, **Spring Security**, **JWT-authenticatie** en een relationele database.  
Het systeem ondersteunt drie typen gebruikers: **Admin**, **Student** en **Cleaner**, elk met specifieke autorisaties.

---

## Tech Stack

| Technologie           | Beschrijving                               |
|-----------------------|--------------------------------------------|
| Java 21               | Kernprogrammeertaal                        |
| Spring Boot 3         | Framework voor REST API’s                  |
| Spring Security + JWT | Authenticatie & autorisatie                |
| Maven**               | Dependency management                      |
| H2 / MySQL            | In-memory en productie database            |
| JPA & Hibernate       | ORM, repository‑laag                       |
| IntelliJ IDEA         | IDE voor ontwikkeling                      |

---

## Rollen & Autorisaties

| Rol     | Toegang                                                        |
|---------|----------------------------------------------------------------|
| ADMIN   | Beheer van gebruikers, facturen, betalingen, schoonmaaktaken   |
| STUDENT | Inzien van eigen facturen, documenten en betalingen            |
| CLEANER | Bekijken en afvinken van toegewezen schoonmaaktaken            |

**JWT Payload bevat o.a.:**
- sub → e‑mailadres  
- role → ROLE_ADMIN / ROLE_STUDENT / ROLE_CLEANER  

---

## Applicatie lokaal starten

**mvn clean spring-boot:run**

**Default endpoints:**
- API root: https://localhost:8443
- H2-console: https://localhost:8443/h2-console
  *JDBC URL:* jdbc:h2:mem:villa

---

## Postman Collectie

Een volledige Postman‑collectie is aanwezig in:

>/postman/villa-vredestein-backend.postman_collection.json**

---

## API Endpoints

### AUTH

| Methode | Endpoint                     | Beschrijving                       |
|---------|------------------------------|------------------------------------|
| POST    | /api/auth/login              | Inloggen met e-mail en wachtwoord  |
| GET     | /api/auth/validate?token=    | Valideer JWT-token                 |

---

### ADMIN Endpoints

| Methode | Endpoint	                                | Beschrijving                     |
|---------|------------------------------------------|----------------------------------|
| GET     | /api/admin/users                         | Alle gebruikers ophalen          |
| PUT     | /api/admin/users/{id}/role?newRole=      | Gebruikersrol wijzigen           |
| DELETE  | /api/admin/users/{id}                    | Gebruiker verwijderen            |
| GET     | /api/admin/invoices                      | Facturen ophalen                 |
| POST    | /api/admin/invoices                      | Nieuwe factuur aanmaken          |
| PUT     | /api/admin/invoices/{id}/status          | Factuurstatus wijzigen           |
| DELETE  | /api/admin/invoices/{id}                 | Factuur verwijderen              |
| GET     | /api/admin/cleaning/tasks                | Schoonmaaktaken beheren          |
| POST    | /api/admin/cleaning/tasks                | Nieuwe taak toevoegen            |

---

### STUDENT Endpoints

| Methode | Endpoint	                     | Beschrijving                     |
|---------|-------------------------------|----------------------------------|
| GET     | /api/student/invoices	        | Eigen facturen ophalen           |
| GET     | /api/student/documents	       | Documentenlijst                  |
| GET     | /api/student/documents/{id}   | Document downloaden              |
| GET     | /api/payments/student/{email} | Eigen betalingen bekijken        |

---

### CLEANER Endpoints

| Methode | Endpoint	                       | Beschrijving                      |
|---------|-----------------------------------|-----------------------------------|
| GET     | /api/cleaning/tasks	              | Eigen taken ophalen               |
| PUT     | /api/cleaning/tasks/{id}/toggle   | Taak markeren als voltooid        |

---

## DTO Structuur

- **LoginRequestDTO** / **LoginResponseDTO**
- **UserResponseDTO** / **UserUpdateDTO**
- **InvoiceRequestDTO** / **InvoiceResponseDTO**
- **CleaningRequestDTO** / **CleaningResponseDTO**
- **UploadResponseDTO**

---

## Automatische E-mails

| Type                    | Trigger         | Ontvanger                           | Beschrijving                 |
|-------------------------|-----------------|--------------------------------------|------------------------------|
| Huurherinnering         | 28e (09:00)     | Studenten met openstaande facturen   | Herinnering vóór deadline    |
| Achterstallige betaling | Dagelijks 09:15 | Studenten met verlopen facturen      | Waarschuwing na vervaldatum  |
| Betalingsbevestiging    | Status = PAID   | Student                              | Bevestiging van betaling     |

**Belangrijke klassen:**
- InvoiceReminderJob
- OverdueInvoiceJob
- MailService
- PaymentService

**Configuratie in:**
>application.yml:

>app:
  mail:
    enabled: true

---

## Projectstructuur

>com.villavredestein
 ┣ config         → SecurityConfig, MailConfig
 ┣ controller     → Auth, Admin, Student, Cleaner, Payment
 ┣ dto            → DTO‑objecten
 ┣ jobs           → Scheduled jobs (herinneringen)
 ┣ model          → Entities: User, Invoice, Payment, CleaningTask, Document
 ┣ repository     → JPA repositories
 ┣ security       → JwtService, JwtAuthenticationFilter
 ┗ service        → Businesslogica

---

## Ontwikkelaar

**Manon Keeman**  
Full Stack Developer & Scrummaster (PSM‑1)  
www.manonkeeman.com  
manonkeeman@gmail.com  

> “Villa Vredestein – bouwen, leven en leren onder één dak.”