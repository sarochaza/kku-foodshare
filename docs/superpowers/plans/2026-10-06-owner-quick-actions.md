# Owner quick actions implementation plan

Goal: continue the delivered Phase4.1 QuickPreview with three requested features: inline reservation editing, a prominent My posts shortcut, and stock/offline distribution controls.

Scope: reuse reservation PUT/DELETE, QR renderer, ownership checks, post pessimistic lock, optimistic version and existing Compose project/volumes. Do not rewrite auth, security, QR, maps, routing, deployment or unrelated code.

1. Write backend regression cases for self-only active reservation lookup, inline quantity change, owner-only stock actions, reserved-stock protection, duplicate/stale requests, offline+QR collection and concurrent reservation/offline distribution. Run tests before implementation where available.
2. Add only offline_quantity migration V3 (default 0; existing rows retained). Add stock policy and owner-only stock endpoint with expectedVersion. All stock changes lock the same post as reservations. Update existing stock formula, search and completion conditions to include offline count.
3. Add direct inline reservation widget (stepper, save/cancel, original QR). Fetch active reservation by post and member, even when full. Never create another reservation when one exists. Fail closed when lookup unavailable. Preserve new-booking trip check.
4. Add signed-in My posts shortcut above page content on all screen sizes. Add expandable owner quantity controls with explicit action, preview, confirmation, stale-data refresh and offline correction.
5. Run available JavaScript/UI/policy tests, Java syntax/build checks and original Maven suite. Record environmental blocks accurately. Review scope diff and package ZIP without .env, build artifacts or dependencies. Save notes and ZIP.

Review focus: simultaneous owner/member changes; repeated submission after network loss; ownership and pickup-code privacy; full/scheduled/expired states; correcting offline entries; mobile 320px; in-flight async updates; original QR scanner code unchanged.
