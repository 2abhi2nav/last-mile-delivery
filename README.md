# Last-Mile Delivery Tracker & Logistics Platform
A production-ready, full-stack Last-Mile Delivery Tracking and Logistics Management Platform built with **Spring Boot 3 (Java 21)**, **PostgreSQL**, **Spring Security (JWT RBAC)**, and **React (Vite)**.

---

## Architecture & Tech Stack
- **Backend**: Spring Boot 3, Spring Web, Spring Data JPA / Hibernate, Spring Security (Stateless JWT), Flyway DB Migrations.
- **Database**: PostgreSQL (Production/Docker), H2 (In-memory testing).
- **Frontend**: React (Vite) decoupled REST client.
- **Roles**: `CUSTOMER`, `DELIVERY_AGENT`, `ADMIN` — enforced via `@PreAuthorize` at the controller level.
- **Key Design Principles**:
  - **Immutable Audit Trail**: `order_status_history` and `order_reschedules` are strictly append-only (`@Immutable`), ensuring zero history mutation.
  - **Pure Rate Calculation Engine**: Isolated business logic for zone resolution, volumetric weight, billable weight tiebreaks, B2B/B2C rate card lookup, and COD surcharges — fully admin-configurable, no hardcoded values.
  - **Non-Blocking Event Hooks**: Status changes trigger email & SMS notifications asynchronously (`REQUIRES_NEW`) without blocking or rolling back primary transaction state.

---

## Setup & Running Locally

### Prerequisites
- Java JDK 21+
- Node.js 18+ & npm / pnpm
- Docker & Docker Compose (optional for local PostgreSQL)

### 1. Environment Configuration
Copy `.env.example` to `.env` and configure your database and provider keys:
```bash
cp .env.example .env
```

### 2. Start Database & Backend
```bash
docker-compose up -d db
cd backend
./gradlew bootRun
```

### 3. Start Frontend
```bash
cd frontend
npm install
npm run dev
```

---

## API Documentation & Endpoints

### Auth (`/api/auth`)
- `POST /api/auth/signup` - Register user (Customer, Delivery Agent, Admin)
- `POST /api/auth/signin` - Authenticate and receive JWT token

### Admin Management (`/api/admin`)
- `POST /api/admin/zones` - Create zone
- `POST /api/admin/zone-areas` - Map pincode to zone
- `POST /api/admin/rate-cards` - Create/update rate cards (B2B/B2C, intra/inter-zone)
- `POST /api/admin/cod-surcharges` - Configure COD surcharge per order type
- `GET /api/admin/orders` - List all orders, filterable by `status`, `zone`, `agentId`

### Orders & Charge Preview (`/api/orders`)
- `POST /api/orders/preview` - Calculate charge preview (zone detection, volumetric weight, billable weight tiebreak, rate lookup, COD surcharge)
- `POST /api/orders/confirm` - Persist order after customer acceptance (customer self-service or admin-on-behalf)
- `GET /api/orders/{orderId}/timeline` - Fetch full immutable audit trail
- `GET /api/orders/mine` - Customer's own order list with live status
- `PATCH /api/orders/{orderId}/status/agent` - Agent status update (Picked Up / In Transit / Out for Delivery / Delivered)
- `PATCH /api/orders/{orderId}/status/override` - Admin status override (any status, any order)
- `POST /api/orders/{orderId}/fail` - Log failed delivery, triggers customer notification
- `POST /api/orders/{orderId}/reschedule` - Reschedule delivery with new date/reason, triggers reassignment

### Agents & Assignment (`/api/agents`)
- `POST /api/agents/{agentId}/location` - Update agent live GPS location
- `PATCH /api/agents/{agentId}/availability` - Toggle agent availability (`available` / `busy` / `offline`)
- `POST /api/agents/assign/manual` - Admin manual agent assignment
- `POST /api/agents/assign/auto/{orderId}` - Auto-assign nearest available agent by zone/proximity

---

## Database Schema Diagram
```
[users] 1 ---- * [orders]
[zones] 1 ---- * [zone_areas (pincodes)]
[rate_cards] (origin_zone, destination_zone, order_type: B2B/B2C)
[cod_surcharges] (order_type)
[agents] 1 ---- 1 [users]
[agents] 1 ---- * [agent_locations]
[orders] 1 ---- * [order_status_history] (Immutable audit trail)
[orders] 1 ---- * [order_reschedules]
[orders] 1 ---- * [notification_logs]
```

---

## Rate Calculation Logic
1. **Zone Detection**: pickup and drop pincodes are resolved to zones via `zone_areas`; intra-zone vs inter-zone is determined by whether the resolved zones match.
2. **Volumetric Weight**: `(L × B × H) / 5000` (dimensions in cm, result in kg).
3. **Billable Weight**: `max(actual_weight, volumetric_weight)`.
4. **Rate Lookup**: the correct rate card (B2B or B2C) is selected by order type, then the intra/inter-zone rate is applied.
5. **Shipping Cost**: `base_rate + (billable_weight × per_kg_rate) + cod_surcharge` (surcharge only if payment type is COD).
6. The computed charge is returned as a preview and only persisted to an order once the customer confirms.

---

## Notifications
Email and SMS are sent on every order status change via a non-blocking async hook (`REQUIRES_NEW` transaction boundary), so provider downtime never rolls back a status transition. Attempts are recorded in `notification_logs` for admin visibility.
