// Regression: About page uses the supplied community illustration without a duplicate overlay caption.
const fs = require('node:fs');
const path = require('node:path');
const assert = require('node:assert/strict');

const root = path.resolve(__dirname, '../../code/src/main/resources');
const template = fs.readFileSync(path.join(root, 'templates/about.html'), 'utf8');
const imagePath = path.join(root, 'static/images/foodshare-about-community.png');
const css = fs.readFileSync(path.join(root, 'static/css/about.css'), 'utf8');

assert.ok(fs.existsSync(imagePath), 'supplied About illustration is available in static images');
assert.match(template, /src="\/images\/foodshare-about-community\.png"/, 'About hero references the supplied illustration');
assert.match(template, /alt="[^"]+"/, 'About illustration has descriptive alternative text');
assert.doesNotMatch(template, /<figcaption>/, 'image text is not duplicated by an overlay caption');
assert.match(css, /aspect-ratio:\s*1479\s*\/\s*1063/, 'image frame follows the illustration aspect ratio');
console.log('PASS: About page illustration is referenced, accessible, and displayed without duplicate text.');
