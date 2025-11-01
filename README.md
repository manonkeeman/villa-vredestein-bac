README.md

# 🏠 Villa Vredestein Backend

**Villa Vredestein** is de backend van een full-stack studentenhuisbeheerapplicatie.  
De applicatie is gebouwd in **Spring Boot 3 (Java 21)** met **JWT-authenticatie**, en biedt functionaliteit voor studenten, beheerders en schoonmakers.  

---

## ⚙️ Tech Stack

| Technologie | Beschrijving |
|--------------|---------------|
| Java 21 | Core programmeertaal |
| Spring Boot 3 | Framework voor REST-API’s |
| Spring Security + JWT | Authenticatie en autorisatie |
| Maven | Dependency management |
| H2 / MySQL | Database (afhankelijk van omgeving) |
| Lombok & JPA | Model- en repository-laag |
| IntelliJ IDEA | Ontwikkelomgeving |

---

## 🔐 Rollen & Rechten

| Rol | Toegang |
|-----|----------|
| **ADMIN** | Beheert gebruikers, facturen, schoonmaakschema’s |
| **STUDENT** | Bekijkt eigen facturen en documenten |
| **CLEANER** | Kan toegewezen schoonmaaktaken markeren of aanpassen |

---

## 🚀 Start de applicatie lokaal

```bash
mvn clean spring-boot:run

De API draait dan op:
👉 http://localhost:8080￼

Je kunt testen via Postman of je frontend.

⸻

🧪 API Endpoints (overzicht)

🔑 AUTH
Methode
Endpoint
Beschrijving
POST
/api/auth/login
Inloggen met email en wachtwoord
GET
/api/auth/validate?token=
Controleer geldigheid van een token

👩‍💼  ADDMIN

Methode
Endpoint
Beschrijving
GET
/api/admin/users
Alle gebruikers ophalen
PUT
/api/admin/users/{id}/role?newRole=
Rol aanpassen
DELETE
/api/admin/users/{id}
Gebruiker verwijderen
GET
/api/admin/invoices
Alle facturen
POST
/api/admin/invoices
Nieuwe factuur aanmaken
PUT
/api/admin/invoices/{id}/status
Status wijzigen
GET
/api/admin/schedules
Overzicht schoonmaakschema’s
POST
/api/admin/schedules/{week}
Nieuw schema aanmaken
POST
/api/admin/schedules/{id}/tasks
Nieuwe taak toevoegen

🧍‍♀️ STUDENT

Methode
Endpoint
Beschrijving
GET
/api/student/invoices
Eigen facturen ophalen
GET
/api/student/documents
Eigen documenten bekijken
GET
/api/student/documents/{id}
Document downloaden

🧹 CLEANER

Methode
Endpoint
Beschrijving
GET
/api/cleaner/schedules
Bekijk eigen taken
PUT
/api/cleaner/tasks/{id}/toggle
Markeer taak als voltooid
PUT
/api/cleaner/tasks/{id}/note
Voeg of wijzig notitie bij taak

📦 DTO Structuur

	•	LoginRequestDTO / LoginResponseDTO
	•	UserResponseDTO / UserUpdateDTO
	•	InvoiceRequestDTO / InvoiceResponseDTO
	•	CleaningRequestDTO / CleaningResponseDTO
	•	UploadResponseDTO

⸻

🧩 Packages

com.villavredestein
 ┣ 📁 config              → SecurityConfig, FakeMailConfig (voor test)
 ┣ 📁 controller          → AuthController, AdminController, StudentController, CleanerController, UserController
 ┣ 📁 dto                 → Alle Data Transfer Objects
 ┣ 📁 model               → Entity-klassen: User, Invoice, CleaningTask, Document, etc.
 ┣ 📁 repository          → Spring Data JPA repositories
 ┣ 📁 security            → JwtService, JwtAuthenticationFilter
 ┗ 📁 service             → Businesslogica: UserService, CleaningService, InvoiceService, DocumentService


🧠 Ontwikkelaar

Manon Keeman
Full Stack Developer & Digital Storyteller
🌍 manonkeeman.comull Stack Developer & Digital Storyteller
🌍 manonkeeman.com￼
📧 manonkeeman@gmail.com
⸻

🖤 Villa Vredestein – bouwen, leven en leren onder één dak.
