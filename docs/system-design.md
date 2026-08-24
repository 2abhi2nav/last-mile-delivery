# System Design Write-up: Last-Mile Delivery Tracker

## 1. Core Architecture & Philosophy
The Last-Mile Delivery Tracking Platform is architected for strict domain separation, data integrity, and fault tolerance across dispatch, pricing, and tracking lifecycles.

## 2. Rate Engine & Zone Detection
- **Zone Resolution**: Pincodes are mapped to discrete geographic zones via the `zone_areas` repository. During order creation and charge preview, pickup and drop pincodes are resolved to `originZone` and `destinationZone`.
- **Volumetric Weight Calculation**: To account for bulky lightweight packages, volumetric weight is computed using standard logistics dimensions:
  $$\text{Volumetric Weight (kg)} = \frac{\text{Length (cm)} \times \text{Breadth (cm)} \times \text{Height (cm)}}{5000}$$
- **Billable Weight Tiebreak**: The billable weight is determined as $\max(\text{actual\_weight}, \text{volumetric\_weight})$, ensuring accurate carrier cost reflection.
- **Dynamic Pricing**: Rates are queried from configurable rate cards matching origin/destination zone pairs and service tiers (`STANDARD` / `EXPRESS`), with configurable COD surcharges applied conditionally.

## 3. Auto-Assignment & Proximity Dispatch
- Agents maintain live availability status (`available`/`busy`/`offline`) and real-time geospatial coordinates (`latitude`, `longitude`) via `agent_locations`.
- The auto-assignment engine queries available agents. When no agents are available, the system throws a controlled exception keeping the order in a queued state (`CREATED`) rather than failing silently or causing data corruption.

## 4. Failed Delivery & Reschedule Flow
- When an agent marks a delivery as failed, the system unassigns the agent, logs an immutable history record, and triggers an asynchronous notification.
- Rescheduling captures a new delivery date and reason in an append-only `order_reschedules` table, preserving full auditability of delivery attempts before re-running auto-assignment.

## 5. Immutable Audit Trail & Non-Blocking Notifications
- **Append-Only History**: `order_status_history` is marked `@Immutable` at the JPA level and insert-only at the repository layer. Status changes never overwrite existing history rows.
- **Non-Blocking Notifications**: Email and SMS notifications operate on an independent transaction boundary (`REQUIRES_NEW`), ensuring that downstream notification provider timeouts or failures never roll back primary order state transitions.
