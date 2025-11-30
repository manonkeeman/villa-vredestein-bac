# Villa Vredestein – Backend Web-API

Een beveiligde, modulair opgebouwde **Spring Boot** web-API voor het beheren van een studentenhuis.  
De applicatie ondersteunt drie gebruikersrollen – **ADMIN**, **STUDENT** en **CLEANER** – elk met hun eigen rechten.

De API voorziet in:

- gebruikersbeheer  
- huurbetalingen & facturen  
- schoonmaaktaken  
- documentbeheer (upload/download)  
- automatische e-mailherinneringen  
- JWT-gebaseerde authenticatie  

---

## Inhoudsopgave

1. Inleiding  
2. Belangrijkste functionaliteiten  
3. Projectstructuur  
4. Technische stack  
5. Installatiehandleiding  
   - 5.1 Benodigdheden  
   - 5.2 Project lokaal installeren  
   - 5.3 Configuratie (JWT, Mail, Database, environment)  
   - 5.4 Applicatie starten  
6. Database & data loading  
7. Rollen & autorisaties  
8. Bestanden uploaden & downloaden  
9. Tests uitvoeren  
10. Error handling  
11. API-documentatie  
12. Postman-collectie  
13. Ontwikkelaar  

---

## 1. Inleiding

De backend van **Villa Vredestein** vormt de basis voor een digitaal beheersysteem van een studentenhuis in Driebergen.  
De applicatie biedt gescheiden toegang voor studenten, schoonmakers en beheerders via een beveiligde REST API.

De API is ontworpen volgens:

- **REST-principes**  
- **CLEAN code**  
- **SOLID-principes**  
- **laag-voor-laag architectuur** (controller → service → repository → database)

---

## 2. Belangrijkste functionaliteiten

- JWT-authenticatie en role-based autorisatie  
- Beheer van **gebruikers**, **facturen**, **betalingen**, **documenten** en **schoonmaaktaken**  
- Uploaden en downloaden van documenten  
- Automatische geplande factuur herinneringen   
- Drie rollen: **ADMIN**, **STUDENT**, **CLEANER**  
- Exception handling met nette foutboodschappen  
- Relationele H2-database met one-to-one en one-to-many relaties  
- Uitgebreide testset:
  - 100% line coverage op twee serviceklassen  
  - Integratietests op scheduled jobs  
  - Security tests via WebMvc / MockMvc  

---

## 3. Projectstructuur

`com.villavredestein`

- `config` – SecurityConfig, CORS, Mail-configuratie  
- `controller` – REST-controllers, endpoints per rol  
- `dto` – Request/Response DTO’s  
- `jobs` – Scheduled jobs (factuur herinneringen)  
- `model` – Entiteiten: `User`, `Invoice`, `Payment`, `CleaningTask`, `Document`, `Room`  
- `repository` – Spring Data JPA repositories  
- `security` – JWT-filter, `JwtService`, `UserDetailsServiceImpl`  
- `service` – Domeinlogica per module  

---

## 4. Technische stack

| Technologie                | Rol                            |
|---------------------------|--------------------------------|
| **Java 21**               | Programmeertaal (LTS)          |
| **Spring Boot 3**         | Web-API framework              |
| **Spring Security + JWT** | Authenticatie & autorisatie    |
| **Maven**                 | Build & dependency management  |
| **H2 Database**           | In-memory database voor testen |
| **JPA/Hibernate**         | ORM-laag                       |
| **JavaMailSender**        | E-mailverzending               |
| **JUnit 5 + Mockito**     | Testframeworks                 |

---

## 5. Installatiehandleiding

### 5.1 Benodigdheden

- Java **17 of 21**  
- Maven **3.8+**  
- Git  
- Postman  

---

### 5.2 Project lokaal installeren

```
git clone https://github.com/manonkeeman/villa-vredestein-bac.git
cd villa-vredestein-bac
mvn clean install
```

---

### 5.3 Configuratie (JWT, Mail, Database, environment)

De basisconfiguratie staat in `src/main/resources/application.yml`.  
Typische instellingen:

```yaml
jwt:
  secret: ${JWT_SECRET:changeme}
  expiration: 3600000    

app:
  mail:
    enabled: false       # zet true om echte mails te versturen
    from: ${MAIL_FROM:noreply@villavredestein.com}
    bcc:
      admin: ${MAIL_BCC_ADMIN:villavredestein@gmail.com}
```

#### Environment-variabelen / `.env`

In productie (of op platforms zoals Render) worden gevoelige waarden **niet** hardcoded, maar als environment-variabelen ingesteld, bijvoorbeeld:

- `JWT_SECRET` – geheime sleutel voor JWT signing  
- `MAIL_FROM` – afzenderadres  
- `MAIL_BCC_ADMIN` – bcc-adres voor beheerder  

Spring Boot leest deze variabelen automatisch in via `${VARIABELE_NAAM:default}`.  
Een eventueel `.env`-bestand wordt *alleen* gebruikt door tooling/deployment en staat bewust niet in versiebeheer.

#### Databaseconfiguratie (H2)

Standaard wordt een in-memory H2 database gebruikt:

- H2-console: `http://localhost:80334/h2-console`  
- JDBC URL: `jdbc:h2:mem:villa`  
- User: `sa`  
- Password: *(leeg)*  

---

### 5.4 Applicatie starten

```
mvn spring-boot:run
```

De API draait vervolgens op:

- `http://localhost:8443`

---

## 6. Database & data loading

Bestand: `src/main/resources/data.sql`

Bij het opstarten worden automatisch voorbeeldgegevens geladen, waaronder:

- gebruikers (ADMIN, STUDENT, CLEANER)  
- facturen en betalingen  
- documenten  
- schoonmaaktaken  
- kamers (Room–User relatie)  

Deze data is bedoeld als test- en demodata.

---

## 7. Rollen & autorisaties

| Rol     | Toegang                                                                  |
|---------|--------------------------------------------------------------------------|
| ADMIN   | Beheer van gebruikers, facturen, betalingen, documenten, schoonmaaktaken |
| STUDENT | Inzien van eigen facturen, documenten en betalingen                     |
| CLEANER | Inzien van en aftekenen van toegewezen schoonmaaktaken                  |

Opmerking: concrete testgebruikers en wachtwoorden worden automatisch geladen via `data.sql`, maar worden bewust niet in deze README gepubliceerd.

---

## 8. Bestanden uploaden & downloaden

**Upload document (ADMIN)**  
- `POST /api/admin/documents/upload`  
- Multipart upload van een bestand, inclusief metadata (titel, beschrijving, roltoegang).

**Download document (STUDENT)**  
- `GET /api/student/documents/{id}`  
- Download van een eerder geüpload document waarvoor de student rechten heeft.

Ondersteunde bestandstypen (voorbeelden):

- PDF  
- afbeeldingen (JPG/PNG)  
- tekst- en officebestanden  

---

## 9. Tests uitvoeren

Alle tests in één keer uitvoeren:

```
mvn test
```

Belangrijkste testsoorten:

- Unit tests met Mockito  
- `InvoiceServiceTest` – 100% line coverage  
- `MailServiceTest` – 100% line coverage  
- Integratietests:
  - `InvoiceReminderJobTest`  
  - `InvoiceReminderJobSeededTest`  
  - `OverdueInvoiceJobTest`  
- Securitytests met Spring WebMvc / MockMvc  

---

## 10. Error handling

Globale exception-afhandeling via `@ControllerAdvice`.  
Fouten worden geretourneerd als gestructureerde JSON-responses.

**Voorbeeld `404 Not Found`:**

```
{
  "status": 404,
  "error": "Not Found",
  "message": "Factuur niet gevonden: 5"
}
```

**Voorbeeld `403 Forbidden`:**

```
{
  "status": 403,
  "error": "Forbidden",
  "message": "STUDENT mag dit niet uitvoeren"
}
```

**Voorbeeld `400 Bad Request`:**

Wordt gebruikt bij validatiefouten in DTO’s (bijv. ontbrekende verplichte velden).

---

## 11. API-documentatie

Uitgebreide API-documentatie met:

- alle endpoints  
- vereiste rollen per endpoint  
- request- en response voorbeelden  
- foutcodes en validatie  

is opgenomen als:

- `docs/API-DocumentatieMVKeeman.pdf`

---

## 12. Postman-collectie

Een Postman-collectie met alle endpoints is aanwezig in:

- `postman/villa-vredestein-backend.postman_collection.json`

Deze collectie kan direct worden geïmporteerd in Postman voor het testen van de API.

---

## 13. Ontwikkelaar

**Manon Keeman**  
Full Stack Developer & Scrummaster (PSM‑1)  
(https://www.manonkeeman.com)  
manonkeeman@gmail.com  

> “Villa Vredestein – bouwen, leven en leren onder één dak.”