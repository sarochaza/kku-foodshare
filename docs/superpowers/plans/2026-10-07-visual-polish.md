# Phase 9 Visual Polish Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Polish the public landing page and signed-in navigation/profile presentation while preserving all existing routes and application behavior.

**Architecture:** Make presentation-only changes in Thymeleaf templates and the existing CSS files. Add source-level Node regression assertions for navigation placement, removed profile copy, mobile landing simplification, and reduced-motion support.

**Tech Stack:** Thymeleaf, CSS, Node built-in test runner.

**Spec:** Approved UI direction in the current conversation.

## Global Constraints

- Do not change authentication, routes, reservation logic, APIs, or persistence.
- Keep the desktop landing layout and existing feature links available.
- Keep mobile navigation distinct and the share action centered.
- Respect `prefers-reduced-motion: reduce`.

## Review Focus

- Signed-in mobile users: About moves out of the bottom bar; `/account/posts` remains reachable there.
- Guests: About remains reachable in the header and landing content remains usable.
- Narrow screens: header actions and bottom navigation must not overflow.
- Reduced-motion preference: animated background and floating elements stop.
- Profile page: removing the heading must not leave excess vertical gap.

---

### Task 1: Lock presentation requirements with regression tests

**Files:**
- Modify: `code/src/test/js/signed-in-home.test.mjs`
- Modify: `code/src/test/js/guest-home.test.mjs`

- [ ] Assert signed-in mobile nav contains a distinct posts link, no About item, and About is in header actions.
- [ ] Assert the three requested profile heading strings are absent.
- [ ] Assert mobile landing simplification and reduced-motion styles are present.
- [ ] Run targeted tests and verify new assertions fail before product changes.

### Task 2: Apply the visual polish

**Files:**
- Modify: `code/src/main/resources/templates/fragments.html`
- Modify: `code/src/main/resources/templates/account.html`
- Modify: `code/src/main/resources/static/css/welcome.css`
- Modify: `code/src/main/resources/static/css/feed-polish.css`

- [ ] Move About to header actions, replace signed-in mobile About with My Posts, and give About a distinct information icon.
- [ ] Remove the three profile heading lines and tighten the profile panel spacing.
- [ ] Simplify only the mobile welcome hero, keep the desktop composition, and add subtle background motion with reduced-motion support.
- [ ] Run all JavaScript regression tests, JavaScript syntax checks, and inspect the final diff.
