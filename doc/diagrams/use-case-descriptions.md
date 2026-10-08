# Use Case descriptions

| Use case | Actor / precondition | Main flow | Alternate flow / result |
|---|---|---|---|
| Browse/search/map | Guest or Member | Query/category/sort → catalog → food preview | Missing GPS: manual location; no result: empty list |
| Register/login | Guest | Submit validated data → account/session | Duplicate email or invalid credentials rejected |
| Reset password | Guest; email provider enabled | Request → expiring token → new password | Invalid/used/expired token rejected; throttled request rejected |
| Profile/onboarding | Active Member | Read/edit profile; finish/skip guide saves account flag | Guide-save error lets user continue; flag can be retried |
| Manage food post | Active Member as Owner | Create/edit food, location, gallery or close | Forbidden owner, invalid time/quantity, existing booking restrictions |
| Reserve food | Active Member who is not Owner | Lock post → check stock/limit → save booking and code | Duplicate retry reuses original; conflicting quantity/key gives 409 |
| Change/cancel booking | Reservation participant with required permission | Check state → change delta or return reserved quantity | Finished booking rejects mutable action; repeat cancellation is idempotent |
| Collect food | Food-post Owner | Scan/manual code → check authorization/time/code → collect | Wrong code increments attempts; excessive attempts temporarily lock |
| Adjust offline stock | Food-post Owner | Submit action/amount/version → lock → adjust free stock | Cannot consume reserved stock; stale version rejected |
| Comment/save/report | Active Member | Publish discussion, save food or submit reasoned report | Validate body/target; ownership checks protect removal |
| Notifications/preferences | Active Member | Read inbox/badge, mark read, edit interests | Pickup reminder ignores ended bookings and avoids duplicate notice |
| Moderation | Active Admin | Review reports, close post or suspend user with reason | Non-admin rejected; actions recorded in audit table |
