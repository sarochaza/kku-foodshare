# Phase 8: Notification styling and guest home navigation

- Reservation activity for the post owner now identifies the booker's public display name and the exact food post. The notification links to that post's detail/owner actions.
- Reservation, comment, and interested-food notifications have distinct blue, green, and amber accents with Thai type labels. Existing notification preferences, unread state, and API fields are retained.
- Logout continues to return to `/`. The public landing page is available at `/`; opening `/home` as a guest now returns to `/` instead of showing the login page. Authenticated `/home` still shows the account dashboard.
- No database schema, reservation rules, stock handling, or Docker Compose configuration was changed.
