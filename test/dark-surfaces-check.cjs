const { chromium } = require('playwright');
const fs = require('node:fs');
const assert = require('node:assert/strict');
(async () => {
  const browser = await chromium.launch({headless:true, executablePath:process.env.CHROMIUM_PATH || undefined, args:["--no-sandbox", "--disable-dev-shm-usage"]});
  try {
    const page = await browser.newPage();
    const css = ['app.css','feed-polish.css','auth-polish.css','theme.css'].map(n=>fs.readFileSync('code/src/main/resources/static/css/'+n,'utf8')).join('\n');
    await page.setContent(`<html data-theme="dark"><head><style>${css}</style></head><body><main class="container"><form class="search-toolbar"><label class="search-input"><input placeholder="ค้นหาอาหาร"></label></form><button class="chip">อาหาร</button><span class="owner-filter"><button>ทั้งหมด</button></span><div class="view-switch"><button class="active">รายการ</button></div><button class="icon-button notification-bell">แจ้งเตือน</button><section class="empty-state"><h3>ไม่มีรายการ</h3></section><section class="panel"><form class="admin-post-filters"><label>ค้นหา<input></label><label>สถานะ<select><option>ทั้งหมด</option></select></label><button>ค้นหา</button></form></section></main></body></html>`);
    for(const width of [390,1440]) {
      await page.setViewportSize({width,height:900});
      for(const selector of ['.search-input','.chip','.owner-filter','.view-switch','.notification-bell','.empty-state']) {
        const bg = await page.locator(selector).evaluate(el=>getComputedStyle(el).backgroundColor);
        assert.notEqual(bg,'rgb(255, 255, 255)',selector+' must not remain white in dark mode');
      }
      assert.ok(await page.evaluate(()=>document.documentElement.scrollWidth <= innerWidth),'no horizontal overflow at '+width);
    }
    console.log('Dark surfaces and responsive layout passed');
  } finally { await browser.close(); }
})().catch(e=>{console.error(e);process.exitCode=1;});
