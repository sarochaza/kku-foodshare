# Progress — plan: 2026-10-06-owner-quick-actions.md

Baseline: exact copy of working Phase4.1 QuickPreview; no Git repository supplied. Isolated copy phase5.
Ruling: offline distribution needs one new zero-default column and a guarded owner stock endpoint — totals cannot be safely stored in browser state — existing rows/API fields and deployment preserved.
Pre-flight: reservation change/cancel continues using existing endpoints; stock endpoint shares post lock/version and available-stock formula with reservation service; UI consumes self-only active lookup and stock snapshot.

Backend: Maven 55 tests passed, 0 failures (includes 6 new integration/migration tests); stock pure rules compiled via JDK compiler module and passed exhaustive checks.
Browser: quick-actions journey passes booking/stock/version/QR/mobile checks with deterministic external map/route fixtures.
Final review: independent reviewer found two Important async cases in quick-actions.js.
Final: fixed confirmation snapshot race — snapshot-version and late-refresh tests RED→GREEN.
Final: fixed lost-booking-response key reuse after cancellation — create-response-loss/rebook test RED→GREEN.

Ruling: original browser regression exposed a pre-existing homepage 360px overflow from .home-now's absolute checkbox (offsetParent BODY, scrollWidth 450). Add only position:relative to the existing label — keeps the filter in its scroller without changing behavior. Cost if wrong: revert one CSS declaration. Dedicated browser test reproduces RED before fix.

Final verification: Maven55/55; Node28/28; JAR package SUCCESS; quick browser14 checks/13 viewport combinations; original browser17 checks/20 combinations; homepage scroller360px check PASS. No browser pageerrors. QR camera/image/code implementation and deployment files preserved.
