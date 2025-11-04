# Villa Vredestein Backend

Villa Vredestein is de backend van een full-stack studentenhuisbeheerapplicatie.  
De applicatie is gebouwd in **Spring Boot 3 (Java 21)** met **JWT-authenticatie**, en biedt functionaliteit voor studenten, beheerders en schoonmakers.

---

## Tech Stack

| Technologie | Beschrijving |
|--------------|---------------|
| **Java 21** | Core programmeertaal |
| **Spring Boot 3** | Framework voor REST API’s |
| **Spring Security + JWT** | Authenticatie en autorisatie |
| **Maven** | Dependency management |
| **H2 / MySQL** | Database (afhankelijk van omgeving) |
| **Lombok & JPA** | Model- en repository-laag |
| **IntelliJ IDEA** | Ontwikkelomgeving |

---

## Rollen & Rechten

| Rol | Toegang |
|------|----------|
| **ADMIN** | Beheert gebruikers, facturen, betalingen en schoonmaakschema’s |
| **STUDENT** | Bekijkt eigen facturen, documenten en betalingen |
| **CLEANER** | Kan toegewezen schoonmaaktaken bekijken en voltooien |

---

## Start de applicatie lokaal

```bash
mvn clean spring-boot:run  De API draait standaard op:
👉 https://localhost:8443

Je kunt de endpoints testen via Postman of koppelen aan je frontend.

⸻

API Endpoints 


AUTH
Methode	Endpoint	Beschrijving
POST	/api/auth/login	Inloggen met e-mail en wachtwoord
GET	/api/auth/validate?token=	Controleer geldigheid van een token


ADMIN
Methode	Endpoint	Beschrijving
GET	/api/admin/users	Alle gebruikers ophalen
PUT	/api/admin/users/{id}/role?newRole=	Rol van gebruiker aanpassen
DELETE	/api/admin/users/{id}	Gebruiker verwijderen
GET	/api/admin/invoices	Alle facturen ophalen
POST	/api/admin/invoices	Nieuwe factuur aanmaken
PUT	/api/admin/invoices/{id}/status	Factuurstatus wijzigen
DELETE	/api/admin/invoices/{id}	Factuur verwijderen
GET	/api/admin/cleaning/tasks	Schoonmaaktaken beheren
POST	/api/admin/cleaning/tasks	Nieuwe taak toevoegen

STUDENT
Methode	Endpoint	Beschrijving
GET	/api/student/invoices	Eigen facturen ophalen
GET	/api/student/documents	Eigen documenten bekijken
GET	/api/student/documents/{id}	Document downloaden
GET	/api/payments/student/{email}	Eigen betalingen bekijken

CLEANER
Methode	Endpoint	Beschrijving
GET	/api/cleaning/tasks	Eigen schoonmaaktaken ophalen
PUT	/api/cleaning/tasks/{id}/toggle	Markeer taak als voltooid


DTO Structuur

	•	LoginRequestDTO / LoginResponseDTO
	•	UserResponseDTO / UserUpdateDTO
	•	InvoiceRequestDTO / InvoiceResponseDTO
	•	CleaningRequestDTO / CleaningResponseDTO
	•	UploadResponseDTO

⸻

Automatische e-mails

Villa Vredestein bevat een geautomatiseerd e-mailsysteem dat studenten en beheerders op de hoogte houdt van betalingen en herinneringen. 
Type e-mail	Trigger	Ontvanger	Beschrijving
💸 Huurherinnering	28e van elke maand (09:00)	Studenten met openstaande facturen	Herinnert aan betaling vóór de vervaldatum
Achterstallige betaling	Dagelijks (09:15)	Studenten met verlopen facturen	Waarschuwt dat de betalingstermijn is verstreken
Betalingsbevestiging	Bij Payment met status = PAID	De betreffende student	Bevestigt dat de huurbetaling is ontvangen

Belangrijke klassen:

	•	InvoiceReminderJob → stuurt vriendelijke herinneringen vóór de vervaldatum
	•	OverdueInvoiceJob → stuurt waarschuwingen ná de vervaldatum
	•	MailService → centrale afhandeling van verzending, rolcontrole en logging
	•	PaymentService → verstuurt automatische ontvangstbevestiging bij betaling

Mailinstellingen zijn configureerbaar in application.ym
app:
  mail:
    enabled: true
    from: villavredestein@gmail.com


 E-mail flow

InvoiceService  →  MailService  →  Student
PaymentService →  MailService  →  Student

De mailfunctionaliteit kan eenvoudig worden uitgeschakeld via:

app.mail.enabled: false


Packages

com.villavredestein
 ┣ 📁 config         → SecurityConfig, MailConfig
 ┣ 📁 controller     → AuthController, AdminController, StudentController, CleanerController, PaymentController
 ┣ 📁 dto            → Alle Data Transfer Objects
 ┣ 📁 jobs           → InvoiceReminderJob, OverdueInvoiceJob
 ┣ 📁 model          → Entity-klassen: User, Invoice, Payment, CleaningTask, Document
 ┣ 📁 repository     → Spring Data JPA repositories
 ┣ 📁 security       → JwtService, JwtAuthenticationFilter
 ┗ 📁 service        → Businesslogica: UserService, CleaningService, InvoiceService, PaymentService, DocumentService, MailService


Ontwikkelaar

Manon Keeman
Full Stack Developer & Scrummaster PSM !
manonkeeman.com
manonkeeman@gmail.com


Villa Vredestein – bouwen, leven en leren onder één dak.

---