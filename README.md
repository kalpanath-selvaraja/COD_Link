# CODLink — Courier & Cash-on-Delivery Management Application

A RESTful API for a courier/logistics business that handles parcel delivery and cash-on-delivery (COD) collection — built for the **ITS1114 – Advanced API Development** module (IJSE, HDSE 75).

Covers the full delivery lifecycle: parcel intake → branch transfer → courier assignment → delivery attempts → COD collection → payment reconciliation → invoicing, with an AI chatbot layered on top.

## Tech stack

- **Java 25**, **Spring Boot 4.1.0**
- **Spring Security** + JWT (`jjwt`) — token-based auth, role-based access control
- **Spring Data JPA / Hibernate** — MySQL
- **Bean Validation** for request validation
- **Lombok**
- **Spring AI (Google Gemini)** — AI chatbot assistant
- Maven (wrapper included — no local Maven install needed)

## Domain model

19 entities, normalized relational schema:

`User`, `Customer`, `Courier`, `Branch`, `BranchTransfer`, `Vehicle`, `ServiceZone`, `Address`,
`Parcel`, `Shipment`, `DeliveryAttempt`, `TrackingEvent`,
`CodCollection`, `Payment`, `Invoice`, `PricingRule`, `ReconciliationBatch`,
`Notification`, `AuditLog`

## Roles

`USER`, `ADMIN`, `COURIER`, `RECEPTIONIST` — enforced via Spring Security + JWT claims.

## API surface

| Controller | Handles |
|---|---|
| `AuthController` | Register / login, JWT issuing |
| `UserController` / `AdminController` | User & account management |
| `CustomerController` | Customer records |
| `CourierController` | Courier accounts & assignment |
| `BranchController` | Branches and branch-to-branch transfers |
| `VehicleController` | Fleet |
| `ParcelController` | Parcel intake, status, delivery attempts, tracking |
| `PaymentController` | COD collection & payment records |
| `InvoiceController` | Invoicing / reconciliation |
| `ChatbotController` | Gemini-backed chatbot assistant |

## Setup

Requires JDK 25 and a running MySQL instance.

```bash
git clone https://github.com/kalpanath-selvaraja/COD_Link.git
cd COD_Link
```

Create a `.env` (or `.env.properties`) file in the project root — it's picked up automatically via `spring.config.import` and is already covered by `.gitignore`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/codlink
spring.datasource.username=YOUR_DATABASE_USERNAME
spring.datasource.password=YOUR_DATABASE_PASSWORD

app.default-admin.username=YOUR_PREFERRED_DEFAULT_ADMIN_USERNAME
app.default-admin.password=YOUR_PREFERRED_DEFAULT_ADMIN_PASSWORD

jwt.secret=YOUR_JWT_SECRET_KEY
jwt.expiration=YOUR_JWT_EXPIRATION_MS

spring.ai.google.genai.api-key=YOUR_GEMINI_API_KEY
spring.ai.google.genai.chat.model=YOUR_GEMINI_MODEL
```

> `application.properties` ships with placeholder values only — no real credentials are committed. Note: `spring.datasource.url` isn't in the checked-in `application.properties` at all, so it must be supplied via your `.env` or the app won't start.

### Running the Application

You can start the project using any of the following methods:

* **Using the Terminal (Maven Wrapper):**
  Run the following command in your project root:
  ```bash
  ./mvnw spring-boot:run
  ```
* **Using your IDE (IntelliJ IDEA / Eclipse / VS Code):**
  Open the project in your preferred IDE, let it load the Maven dependencies, locate the main entry point class (`CodLinkApplication.java`), and click the **Run / Debug button** (the green play icon).
* **Using a Pre-packaged JAR:**
  Build the executable package first using `./mvnw clean package`, then run the generated archive file from your terminal:
  ```bash
  java -jar target/codlink-0.0.1-SNAPSHOT.jar
  ```

A `DataSeeder` runs on startup and creates the default admin account from the `app.default-admin.*` values above.

## Status

**Completed** — This project was developed as a final coursework submission for the *ITS1114 – Advanced API Development* module and is no longer actively maintained. 
