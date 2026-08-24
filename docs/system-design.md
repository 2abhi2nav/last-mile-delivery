# System Design Write-up: Last-Mile Delivery Tracker

## 1. Core Architecture & Philosophy
The Last-Mile Delivery Tracking Platform is architected for strict domain separation, data integrity, and fault tolerance across dispatch, pricing, and tracking lifecycles. Business logic (rate calculation, assignment, status transitions) is isolated in stateless service layers, decoupled from persistence and notification concerns, so each can be tested and reasoned about independently.

## 2. Rate Engine & Zone Detection
- **Zone Resolution**: Pincodes are mapped to discrete geographic zones via the `zone_areas` repository. During order creation and charge preview, pickup and drop pincodes are resolved to `originZone` and `destinationZone`.
- **Volumetric Weight Calculation**: To account for bulky lightweight packages, volumetric weight is computed using standard logistics dimensions:
  `volumetric_weight_kg = (length_cm × breadth_cm × height_cm) / 5000`
- **Billable Weight Tiebreak**: The billable weight is determined as `max(actual_weight, volumetric_weight)`, ensuring accurate carrier cost reflection regardless of whether a package is dense or bulky.
- **Dynamic Pricing**: Rates are queried from configurable rate cards matching origin/destination zone pairs and service tiers (`STANDARD` / `EXPRESS`), with configurable COD surcharges applied conditionally. No rate value is hardcoded — all pricing is admin-managed and versioned at the rate-card level, so pricing changes never require a deployment.

## 3. Auto-Assignment & Proximity Dispatch
- Agents maintain live availability status (`available` / `busy` / `offline`) and real-time geospatial coordinates (`latitude`, `longitude`) via `agent_locations`.
- The auto-assignment engine queries available agents within the destination zone, ranks by proximity, and assigns the nearest match.
- When no agents are available, the system throws a controlled exception keeping the order in a queued state (`CREATED`) rather than failing silently or causing data corruption. Admins can retry assignment manually once agents free up.

## 4. Failed Delivery & Reschedule Flow
- When an agent marks a delivery as failed, the system unassigns the agent, logs an immutable history record, and triggers an asynchronous notification to the customer.
- Rescheduling captures a new delivery date and reason in an append-only `order_reschedules` table, preserving full auditability of every delivery attempt before re-running auto-assignment for the new date.
- This design allows an order to accumulate multiple failed attempts over time without losing the history of what was tried, when, and why.

## 5. Immutable Audit Trail & Non-Blocking Notifications
- **Append-Only History**: `order_status_history` is marked `@Immutable` at the JPA level and insert-only at the repository layer. Status changes never overwrite existing history rows, giving customers and admins a complete, tamper-evident timeline for every order.
- **Non-Blocking Notifications**: Email and SMS notifications operate on an independent transaction boundary (`REQUIRES_NEW`), ensuring that downstream notification provider timeouts or failures never roll back primary order state transitions. Notification attempts are logged separately so delivery failures are visible to admins without affecting order integrity.
