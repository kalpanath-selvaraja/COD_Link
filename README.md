# CODLink - Courier & Cash-on-Delivery Management Application

Coursework for ITS1114 - Advanced API Development (IJSE, HDSE 75).

CODLink handles parcel booking, courier claims, delivery attempts, branch transfers,
COD collection, service-charge receipts and reconciliation. Spring Boot serves the
REST API and the static jQuery pages from the same origin.

## Tech stack

- Java 25, Spring Boot 4.1.0, Maven wrapper
- Spring Security, BCrypt and JWT
- Spring Data JPA and MySQL 8
- Bean Validation and Lombok
- Direct Google Gemini HTTP integration using RestClient
- JUnit, MockMvc and H2 for isolated tests

## Setup

```bash
git clone https://github.com/kalpanath-selvaraja/COD_Link.git
cd COD_Link
git switch Review
cp .env.example .env
```

On Windows, use `Copy-Item .env.example .env`. Fill in the database, admin, JWT and
Gemini values locally. `.env` uses Java properties syntax and is imported by
`spring.config.import`. `.env.properties` is ignored by Git but is not imported.
The datasource URL in `application.properties` is a placeholder.

Generate a random Base64 JWT secret containing at least 32 random bytes. Keep the
actual `.env` and its values out of commits. Only `.env.example` belongs in Git.

For an existing database, stop the application, take a backup and follow
[the database upgrade steps](database/README.md). Schema validation is the default.
For a new empty development database only, temporarily set
`spring.jpa.hibernate.ddl-auto=update` in `.env` for the first startup, then change
it back to `validate`. Do not use `create` or `create-drop` on existing data.

```bash
./mvnw spring-boot:run
```

Windows: `./mvnw.cmd spring-boot:run`. Open `/login.html` on the application host.
`DataSeeder` creates the first admin using the configured `app.default-admin.*`
values. The entry class is `CodlinkApplication`.

```bash
./mvnw clean package
java -jar target/codlink-0.0.1-SNAPSHOT.jar
```

## Roles and workflows

- USER: own customer profile, COD bookings, own parcels and shipments, notifications.
- COURIER: own branch's available parcels, claims, assigned parcel actions, own
  attempts and collections. Delivery and collection must use the assigned courier.
- RECEPTIONIST: existing shipment list/create/cancel/delete permissions and common
  self-service. The frontend opens the shipment screen. Finance and parcel-management
  screens remain restricted.
- ADMIN: account, reference-data, shipment, assignment, finance and audit management.
  The AI assistant is admin-only.

Online bookings require positive COD, complete addresses and weight above zero up
to 70kg. A cart has at most 20 parcels. Staff can also book prepaid parcels. Cart
booking groups parcels by delivery zone and payment type and creates invoices.
Attaching a parcel requires an active uninvoiced shipment for the same customer,
delivery zone and payment type. Single online requests cannot supply shipment IDs.

Shipping uses half-open weight brackets such as `0-5`, `5-10` and `10+`.
Brackets for the same zone/payment type cannot overlap. Cross-zone pickup adds
15%; a returned parcel adds 110% of its current base shipping rate as a return fee.

Parcel transitions are `BOOKED -> IN_TRANSIT -> DELIVERED/FAILED` and
`FAILED -> IN_TRANSIT/RETURNED`. State changes create tracking events. Manual
tracking entries are notes, not parcel transitions. Transfers update the current
branch immediately, clear assignment and add a transfer event.

## New and changed APIs

Responses retain `CommonResponse(status, body, message)`.

| Route | Behavior |
| --- | --- |
| `GET /api/parcels/available` | Courier's branch, unassigned unresolved parcels |
| `GET /api/parcels/assigned` | Courier's assigned parcels |
| `PATCH /api/parcels/{id}/claim` | Atomic claim; same-courier retry succeeds, another claim returns 409 |
| `PATCH /api/parcels/{id}/courier/{courierId}` | Admin reassignment to an active courier in the current branch |
| `GET /api/parcels/track/{trackingNumber}` | Public tracking number, status and branch names only |
| `GET /api/notification/me` | Latest 50 notifications for the authenticated user |
| `POST /api/payments` | Explicit receipt with invoiceId, amount, method and requestReference |

Invoice responses include `amount`, `paidAmount` and `balance`. Parcel responses
include `assignedCourierId`. Payment responses include `requestReference` and the
linked `collectionId` for COD shipping receipts.

```json
{
  "invoiceId": 301,
  "amount": 100.00,
  "method": "CASH",
  "requestReference": "b8496588-97cb-4c99-a629-41178949aaad"
}
```

Methods are `CASH`, `BANK_TRANSFER` and `CREDIT_CARD`. Reuse the same reference when
retrying the same receipt. A changed payload with that reference returns 409.
Partial receipts are allowed; overpayment is rejected. Uncollected COD shipping
fees are reserved for collection rather than manual payment.

COD collection must match goods value plus shipping. It creates one linked receipt
for shipping only; goods value is not revenue. Prepaid booking and parcel return
do not create payments. Returns update the invoice even after earlier receipts.
A shipment completes only when every parcel is delivered/returned, required COD
has been collected and the invoice balance is zero.

Reconciliation locks the branch and unbatched collections and uses the branch
recorded when cash was collected. Concurrent conflicting requests return 409.
Successful service mutations record audit actions for the authenticated actor.
Booking, assignment, status changes and receipts create in-app notifications.

AI reporting uses current parcel/customer counts and recorded service-charge
receipts for the current week to date and previous complete week. Weeks begin
Monday in Asia/Colombo. Unreviewed legacy payments are excluded from weekly totals.
Gemini has a 5-second connection timeout and 20-second read timeout; failures return 502.

## Checks

```bash
./mvnw test
node scripts/check-frontend.cjs
```

Tests use the `test` profile and an isolated H2 database; `.env` is not loaded.
Business-flow tests cover permissions, booking, concurrent claims/receipts,
returns, reconciliation, audit, notifications and weekly boundaries. For a
disposable MySQL database, the same suite can run with datasource system-property
overrides. The test profile uses `create-drop`; never point it at business data.

The Mermaid source in `src/main/resources/docs/codlink-ERD.mmd` reflects the entity
model. The older exported SVG is a historical image. Branch addresses, vehicle
courier assignment and pricing-zone assignment remain fixed after creation.
