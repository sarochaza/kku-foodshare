import assert from "node:assert/strict";
import { readFile } from "node:fs/promises";
import test from "node:test";
import { feedCard } from "../../main/resources/static/js/ui.js";

const app = await readFile(new URL("../../main/resources/static/js/app.js", import.meta.url), "utf8");
const ui = await readFile(new URL("../../main/resources/static/js/ui.js", import.meta.url), "utf8");
const exploreTemplate = await readFile(new URL("../../main/resources/templates/explore.html", import.meta.url), "utf8");
const css = await readFile(new URL("../../main/resources/static/css/app.css", import.meta.url), "utf8");

test("feed uses the multi-image gallery and comment API", () => {
  assert.match(ui, /export function gallery/);
  assert.match(ui, /data-report-post/);
  assert.match(app, /food-posts\/\$\{postId\}\/comments/);
});

test("food feed presents title, image, and description together in that order", () => {
  const template = ui.slice(ui.indexOf('export function feedCard'));
  const head = template.indexOf('food-feed-head');
  const image = template.indexOf('${gallery(p)}', head);
  const description = template.indexOf('feed-description', image);
  assert.ok(head >= 0 && image > head && description > image);
});

test("food post cards show road distance only when the route service provided it", () => {
  const html = feedCard({
    id: 1, category: "FOOD", title: "ข้าว", pickupLocationName: "คณะวิทยาศาสตร์",
    saved: false, images: [], ownerId: 2, ownerName: "ผู้แบ่งปัน", commentCount: 0,
    status: "AVAILABLE", availableQuantity: 2, unit: "กล่อง", distanceKm: 1.2,
    availableFrom: "2026-10-07T08:00:00+07:00", availableUntil: "2026-10-07T10:00:00+07:00",
  });
  assert.doesNotMatch(html, /1\.2 กม\. จากคุณ/);
  assert.match(html, /data-road-distance="1"/);
  const roadHtml = feedCard({
    id: 1, category: "FOOD", title: "ข้าว", pickupLocationName: "คณะวิทยาศาสตร์",
    saved: false, images: [], ownerId: 2, ownerName: "ผู้แบ่งปัน", commentCount: 0,
    status: "AVAILABLE", availableQuantity: 2, unit: "กล่อง", distanceKm: 1.2, roadDistanceKm: 2.4,
    availableFrom: "2026-10-07T08:00:00+07:00", availableUntil: "2026-10-07T10:00:00+07:00",
  });
  assert.match(roadHtml, /2\.4 กม\. ขับรถจากคุณ/);
});

test("notifications have real preferences and unread API routes", () => {
  assert.match(app, /notification-preferences/);
  assert.match(app, /data-report-comment/);
});

test("notification feed identifies each event type with its own color style", () => {
  assert.ok(app.includes('data-type="${escape(n.type || "UPDATE")}"'));
  for (const label of ['RESERVATION:"การจอง"', 'COMMENT:"คอมเมนต์"', 'INTEREST:"อาหารที่สนใจ"']) assert.ok(app.includes(label));
  assert.match(css, /\.notification-card\[data-type="RESERVATION"\]/);
  assert.match(css, /\.notification-card\[data-type="COMMENT"\]/);
  assert.match(css, /\.notification-card\[data-type="INTEREST"\]/);
});

test("food feed exposes save, owner and the shared route preview", () => {
  assert.match(ui, /data-save-post/);
  assert.doesNotMatch(ui, /feed-map-link|googleMapsUrl\(p\)/);
  assert.match(ui, /availabilityLabel/);
  assert.match(app, /data-owner/);
  assert.match(app, /saved-posts/);
});

test("homepage obtains the current location automatically for food distances and map links", () => {
  assert.match(app, /void useLocation\(\{silent:true\}\)/);
  assert.match(app, /setDefaultOrigin\(position\)/);
});

test("explore map pins open a food preview and expanded map has a control to restore information", () => {
  const explore = app.slice(app.indexOf("async function explore()"), app.indexOf("async function memberProfile()"));
  assert.match(explore, /markers\(map, d\.items, layer, p => showMapPost\(p\)\)/);
  assert.match(explore, /mapFullButton\.textContent = expanded \? "ดูข้อมูล" : "ขยายแผนที่"/);
  assert.match(exploreTemplate, /id="explore-map-preview"/);
});

test("post detail groups title, food gallery, and description in one panel in that order", () => {
  const detail = app.slice(app.indexOf('async function detail()'));
  const title = detail.indexOf('detail-title');
  const image = detail.indexOf('gallery(p, "detail-gallery")');
  const description = detail.indexOf('detail-description');
  const pickupName = detail.indexOf('detail-pickup-name');
  assert.ok(title >= 0 && image > title && description > image && pickupName > description);
  assert.match(detail, /detail-food-content/);
  assert.doesNotMatch(detail, /data-detail-directions|detail-pickup-panel/);
  assert.match(detail, /<div id="detail-trip" class="detail-route-panel"><\/div>/);
});

test("the reservation view does not bypass the shared route preview with a direct Google Maps link", () => {
  const reservationTemplate = app.slice(app.indexOf('function reservationCard'));
  assert.doesNotMatch(reservationTemplate, /googleMapsUrl\(p\)/);
});
