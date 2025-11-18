# Villa Vredestein Backend

Villa Vredestein is de backend van een full-stack studentenhuis beheerapplicatie.  
De applicatie is gebouwd in **Spring Boot 3 (Java 21)** met **JWT-authenticatie**, en biedt functionaliteit voor studenten, beheerders en schoonmakers.

---

## Tech Stack

| Technologie                | Beschrijving                        |
|----------------------------|-------------------------------------|
| **Java 21**                | Core Programmeertaal                |
| **Spring Boot 3**          | Framework voor REST API’s           |
| **Spring Security + JWT**  | Authenticatie en Autorisatie        |
| **Maven**                  | Dependency Management               |
| **H2 / MySQL**             | Database (afhankelijk van omgeving) |
| **JPA & Hibernate**        | Model- en repository-laag           |
| **IntelliJ IDEA**          | Ontwikkelomgeving                   |

---

## Rollen & Rechten

| Rol         | Toegang                                                        |
|-------------|----------------------------------------------------------------|
| **ADMIN**   | Beheert gebruikers, facturen, betalingen en schoonmaakschema’s |
| **STUDENT** | Bekijkt eigen facturen, documenten en betalingen               |
| **CLEANER** | Kan toegewezen schoonmaaktaken bekijken en voltooien           |

JWT-tokens bevatten:
•	sub → e-mailadres
•	role → ROLE_ADMIN / ROLE_STUDENT / ROLE_CLEANER

---

## Start de applicatie lokaal

mvn clean spring-boot:run

De API draait standaard op:
**https://localhost:8443**
**H2-console: https://localhost:8443/h2-console**
(JDBC URL: jdbc:h2:mem:villa)

## POSTMAN COLLECTIE
Voor het testen van alle endpoints is een complete Postman-collectie beschikbaar:
•	/postman/villa-vredestein-backend.postman_collection.json)

---

## API ENDPOINTS 


AUTH
| Methode	| Endpoint	                | Beschrijving                        |
|-----------|---------------------------|-------------------------------------|
| POST	    | /api/auth/login	        | Inloggen met e-mail en wachtwoord   |
| GET	    | /api/auth/validate?token=	| Controleer geldigheid van een token |


ADMIN
| Methode	| Endpoint	                             | Beschrijving           |
|-----------|----------------------------------------|------------------------|
| GET	    | /api/admin/users	                     | Alle gebruikers        |
| PUT	    | /api/admin/users/{id}/role?newRole=    | Rol wijzigen           |
| DELETE	|/api/admin/users/{id}	                 | Gebruiker verwijderen  |
| GET	    |/api/admin/invoices	                 | Alle facturen          |
| POST	    |/api/admin/invoices	                 | Factuur aanmaken       |
| PUT	    |/api/admin/invoices/{id}/status         | Factuurstatus wijzigen |
| DELETE	|/api/admin/invoices/{id}	             | Factuur verwijderen    |
| GET	    |/api/admin/cleaning/tasks	             | Taken beheren          |
| POST	    |/api/admin/cleaning/tasks	             | Nieuwe taak            |

STUDENT
| Methode	| Endpoint	                    | Beschrijving                    |
|-----------|-------------------------------|---------------------------------|
| GET	    | /api/student/invoices	        | Eigen facturen ophalen          |
| GET	    | /api/student/documents	    | Documentenlijst                 |
| GET	    | /api/student/documents/{id}   | Document downloaden             |
| GET	    | /api/payments/student/{email} | Eigen betalingen bekijken       |

CLEANER
| Methode	| Endpoint	                      | Beschrijving                  |
|-----------|---------------------------------|-------------------------------|
| GET	    | /api/cleaning/tasks	          | Eigen taken ophalen           |
| PUT	    | /api/cleaning/tasks/{id}/toggle |	Markeer taak als voltooid     |

---

## DTO STRUCTUUR

	•	LoginRequestDTO    / LoginResponseDTO
	•	UserResponseDTO    / UserUpdateDTO
	•	InvoiceRequestDTO  / InvoiceResponseDTO
	•	CleaningRequestDTO / CleaningResponseDTO
	•	UploadResponseDTO

---

## AUTOMATISCHE E-MAILS

De backend verstuurt automatisch:
| Type                    | Trigger         |Ontvanger                           | Beschrijving               |
|-------------------------|-----------------|------------------------------------|----------------------------|
| Huurherinnering         | 28e (09:00)     | Studenten met openstaande facturen | Betaal vóór de vervaldatum |
| Achterstallige betaling | Dagelijks 09:15 | Studenten met verlopen facturen    | Termijn verstreken         |
| Betalingsbevestiging    | Status = PAID   | Student                            | Ontvangstbevestiging       |


Belangrijke klassen:

	•	InvoiceReminderJob → stuurt vriendelijke herinneringen vóór de vervaldatum
	•	OverdueInvoiceJob → stuurt waarschuwingen ná de vervaldatum
	•	MailService → centrale afhandeling van verzending, rolcontrole en logging
	•	PaymentService → verstuurt automatische ontvangstbevestiging bij betaling

Mailinstellingen zijn configureerbaar in application.yml
app:
  mail:
    enabled: true

---

## PROJECTSTRUCTUUR

com.villavredestein
 ┣ 📁 config         → SecurityConfig, MailConfig
 ┣ 📁 controller     → Auth, Admin, Student, Cleaner, Payment
 ┣ 📁 dto            → Alle Data Transfer Objects
 ┣ 📁 jobs           → InvoiceReminderJob, OverdueInvoiceJob
 ┣ 📁 model          → Entities: User, Invoice, Payment, CleaningTask, Document
 ┣ 📁 repository     → JPA repositories
 ┣ 📁 security       → JwtService, JwtAuthenticationFilter
 ┗ 📁 service        → Businesslogica laag

---

## ONTWIKKELAAR

Manon Keeman
Full Stack Developer & Scrummaster (PSM-1)
www.manonkeeman.com
manonkeeman@gmail.com


"Villa Vredestein – bouwen, leven en leren onder één dak."

---