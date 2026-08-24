# Last-Mile Delivery Tracker & Logistics Platform

A production-ready, full-stack Last-Mile Delivery Tracking and Logistics Management Platform built with **Spring Boot 3 (Java 21)**, **PostgreSQL**, **Spring Security (JWT RBAC)**, and **React (Vite)**.

---

## Architecture & Tech Stack
- **Backend**: Spring Boot 3, Spring Web, Spring Data JPA / Hibernate, Spring Security (Stateless JWT), Flyway DB Migrations.
- **Database**: PostgreSQL (Production/Docker), H2 (In-memory testing).
- **Frontend**: React (Vite) decoupled REST client.
- **Key Design Principles**:
  - **Immutable Audit Trail**: `order_status_history` and `order_reschedules` are strictly append-only (`@Immutable`), ensuring zero history mutation.
  - **Pure Rate Calculation Engine**: Isolated business logic for volumetric weight, billable weight tiebreaks, zone resolution, and COD surcharges.
  - **Non-Blocking Event Hooks**: Status changes trigger email & SMS notification simulations asynchronously without blocking or rolling back primary transaction state.

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
```.bash
cd frontend (or root)
npm install
npm run dev
```

---

## API Documentation & Endpoints

### Auth (`/api/auth`)
- `POST /api/auth/signup` - Register user (Customer, Merchant, Agent, Admin)
- `POST /api/auth/signin` - Authenticate and receive JWT token

### Admin Management (`/api/admin`)
- `POST /api/admin/zones` - Create zone
- `POST /api/admin/zone-areas` - Map pincode to zone
- `POST /api/admin/rate-cards` - Create/update rate cards
- `POST /api/admin/cod-surcharges` - Configure COD surcharge per order type

### Orders & Charge Preview (`/api/orders`)
- `POST /api/orders/preview` - Calculate charge preview (volumetric weight, billable weight tiebreak, rate lookup, COD surcharge)
- `POST /api/orders/confirm` - Persist order after customer acceptance
- `GET /api/orders/{orderId}/timeline` - Fetch full immutable audit trail
- `PATCH /api/orders/{orderId}/status/agent` - Agent status update
- `PATCH /api/orders/{orderId}/status/override` - Admin status override
- `POST /api/orders/{orderId}/fail` - Log failed delivery
- `POST /api/orders/{orderId}/reschedule` - Reschedule delivery with new date/reason

### Agents & Assignment (`/api/agents`)
- `POST /api/agents/{agentId}/location` - Update agent live GPS location
- `PATCH /api/agents/{agentId}/availability` - Toggle agent availability (`available`/`busy`/`offline`)
- `POST /api/agents/assign/manual` - Admin manual agent assignment
- `POST /api/agents/assign/auto/{orderId}` - Auto-assign nearest/available agent

---

## Database Schema Diagram
```
[users] 1 ---- * [orders]
[zones] 1 ---- * [zone_areas (pincodes)]
[rate_cards] (origin_zone, destination_zone, order_type)
[cod_surcharges] (order_type)
[agents] 1 ---- 1 [users]
[agents] 1 ---- * [agent_locations]
[orders] 1 ---- * [order_status_history] (Immutable audit trail)
[orders] 1 ---- * [order_reschedules]
[orders] 1 ---- * [notification_logs]
```

---

## Rate Calculation Formulae
1. **Volumetric Weight**: $\frac{L \times B \times H}{5000}$ (dimensions in cm, result in kg).
2. **Billable Weight**: $\max(\text{actual\_weight}, \text{volumetric\_weight})$.
3. **Shipping Cost**: $\text{base\_rate} + (\text{billable\_weight} \times \text{per\_kg\_rate}) + [\text{cod\_surcharge}]$.
