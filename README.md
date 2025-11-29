# Villa Vredestein – Backend Web-API

Een beveiligde, modulair opgebouwde Spring Boot web-API voor het beheren van een studentenhuis.  
De applicatie ondersteunt drie gebruikersrollen – **ADMIN**, **STUDENT** en **CLEANER** – elk met hun eigen rechten.

De API voorziet in:
- gebruikersbeheer
- huurbetalingen & facturen
- schoonmaaktaken
- documentbeheer (upload/download)
- automatische e-mailherinneringen
- JWT-gebaseerde authenticatie

---

# Inhoudsopgave
1. [Inleiding]
2. [Belangrijkste Functionaliteiten]
3. [Projectstructuur]
4. [Technische Stack]
5. [Installatiehandleiding]
    - 5.1 Benodigdheden
    - 5.2 Project lokaal installeren
    - 5.3 Configuratie (JWT, Mail, Database)
    - 5.4 Applicatie starten
6. [Database & Data Loading]
7. [Rollen & Autorisaties]
8. [Bestanden Uploaden & Downloaden]
9. [Tests uitvoeren]
10. [Error Handling]
11. [API-documentatie]
12. [Postman Collectie]
13. [Ontwikkelaar]

---

**1. INLEIDING**

De backend van **Villa Vredestein** vormt de basis voor een digitaal beheersysteem van een studentenhuis in Driebergen.  
De applicatie biedt gescheiden toegang voor  studenten, schoonmakers en beheerders via een beveiligde API.

De API is ontworpen volgens:
- **REST-principes**
- **CLEAN code**
- **SOLID-principes**
- **laag-voor-laag architectuur**

---

**2. BELANGRIJKSTE FUNCTIONALITEITEN**

- JWT-authenticatie + autorisatie
- Beheer van **gebruikers**, **facturen**, **betalingen**, **documenten** en **schoonmaaktaken**
- Uploaden & downloaden van documenten
- Automatische geplande factuur herinneringen
- 3 rollen: **ADMIN**, **STUDENT**, **CLEANER**
- Exception handling met nette foutboodschappen
- Relationale H2-database met 1-to-1 en 1-to-many relaties
- Uitgebreide testset:
    - 100% line coverage op twee serviceklassen
    - Integratietests op scheduled jobs
    - Security tests via WebMvc

---

**3. PROJECTSTRUCTUUR**

com.villavredestein
┣ config                → SecurityConfig, CORS, Mail config
┣ controller            → Endpoints per rol
┣ dto                   → Request/Response DTO’s
┣ jobs                  → Scheduled jobs (factuur herinneringen)
┣ model                 → Entiteiten: User, Invoice, Payment, CleaningTask, Document
┣ repository            → JPA repositories
┣ security              → JWT-filter, JwtService, UserDetailsServiceImpl
┗ service               → Domeinlogica per module

**4. TECHNISCHE STACK**

| Technologie               | Rol                           |
|---------------------------|-------------------------------|
| **Java 21**               | programmeertaal (LTS)         |
| **Spring Boot 3**         | Web-API framework             |
| **Spring Security + JWT** | Authenticatie & autorisatie   |
| **Maven**                 | Build & dependency management |
| **H2 Database**           | In-memory DB voor testen      |
| **JPA/Hibernate**         | ORM laag                      |
| **JavaMailSender**        | E-mailverzending              |
| **JUnit 5 + Mockito**     | Testframeworks                |

---

**5. INSTALLATIEHANDLEIDING**

## 5.1 BENODIGDHEDEN
- Java **17 of 21**
- Maven 3.8+
- Git
- (optioneel) Postman

---

## 5.2 PROJECT LOKAAL INSTALLEREN

git clone https://github.com/manonkeeman/villa-vredestein-bac.git
cd villa-vredestein-bac
mvn clean install

## 5.3 CONFIGURATIE (JWT, Mail, Database)

Te vinden in: application.yml
jwt:
  secret: ditIsEenVeiligSecret123
  expiration: 3600000

Mailconfiguratie:
  app:
  mail:
    enabled: false         # zet true om echte mails te verzenden
    from: noreply@villavredestein.com
    bcc:
      admin: villavredestein@gmail.com
      
Database configuratie
H2 Console:
•	URL: http://localhost:8443/h2-console
•	JDBC URL: jdbc:h2:mem:villa
•	User: sa
•	Password: leeg

## 5.4 APPLICATIE STARTEN

mvn spring-boot:run

API draait vervolgens op:
→ http://localhost:8443


**6 DATABASE & DATA LOADING**

src/main/resources/data.sql

Automatische laaddata bevat:
•	gebruikers (ADMIN, STUDENT, CLEANER)
•	facturen & betalingen
•	documenten
•	schoonmaaktaken


**7. ROLLEN EN AUTORISATIES**

| Rol     | Toegang                                                        |
|---------|----------------------------------------------------------------|
| ADMIN   | Beheer van gebruikers, facturen, betalingen, schoonmaaktaken   |
| STUDENT | Inzien van eigen facturen, documenten en betalingen            |
| CLEANER | Bekijken en afvinken van toegewezen schoonmaaktaken            |

Opmerking: Testgebruikers en wachtwoorden worden automatisch geladen via data.sql, 
maar worden niet gepubliceerd in deze README.


---

**8. BESTANDEN UPLOADEN & DOWNLOADEN**

Upload document (ADMIN):
POST /api/admin/documents/upload

Download document (STUDENT):
GET /api/student/documents/{id}

Ondersteunt:
	•	PDF
	
**9. TESTEN UITVOEREN**

Alle tests uitvoeren:
mvn test

Testsoorten:
•	Unit tests met Mockito
•	InvoiceServiceTest (100% line coverage)
•	MailServiceTest (100% line coverage)
•	Integratietests:
    •	InvoiceReminderJobTest
    •	OverdueInvoiceJobTest
•	Securitytests
    

	
**10. ERROR HANDLING**

Globale exception handler middels @ControllerAdvice.

Voorbeeld 404 Not Found
{
"status": 404,
"error": "Not Found",
"message": "Factuur niet gevonden: 5"
}

Voorbeeld 403 Forbidden
{
  "status": 403,
  "error": "Forbidden",
  "message": "STUDENT mag dit niet uitvoeren"
}

Voorbeeld 400 Bad Request
Bij validatiefouten in DTO’s.


**11. API-DOCUMENTATIE**

Volledige API-documentatie (endpoints, foutcodes, security):
/docs/api-documentatie.pdf

	
**12 POSTMAN COLLECTIE**

Downloadbare collectie:
>/postman/villa-vredestein-backend.postman_collection.json**

---

**13 ONTWIKKELAAR**

**Manon Keeman**  
Full Stack Developer & Scrummaster (PSM‑1)  
www.manonkeeman.com  
manonkeeman@gmail.com  

---

> “Villa Vredestein – bouwen, leven en leren onder één dak.”