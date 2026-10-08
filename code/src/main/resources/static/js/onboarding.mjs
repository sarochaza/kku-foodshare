// Completion comes from the signed-in account, never shared browser storage.
export function initHomeGuide({
  complete,
  onError = () => {},
  doc = document,
} = {}) {
  const shell = doc.querySelector('#home-guide');
  if (!shell || shell.dataset.initialized) return;

  const replay = new URLSearchParams(
    doc.defaultView.location.search
  ).has('guide');

  const signedIn =
    doc.querySelector('meta[name="signed-in"]')?.content === 'true';

  const needed =
    doc.querySelector('meta[name="onboarding-needed"]')?.content === 'true';

  if (!replay && (!signedIn || !needed)) return;

  shell.dataset.initialized = 'true';

  const home = !!doc.querySelector('#home-search');

  const steps = [
    [
      home ? '#home-search' : '#search-form',
      'ค้นหามื้อที่ถูกใจ',
      'พิมพ์ชื่ออาหารหรือจุดรับ แล้วดูรายการที่เพื่อน ๆ แบ่งปัน',
    ],
    [
      home ? '#home-category-filters' : '#category-filters',
      'เลือกสิ่งที่อยากรับ',
      'กรองอาหาร เครื่องดื่ม ของว่าง หรือเฉพาะรายการที่รับได้ตอนนี้',
    ],
    [
      home ? '#home-map' : '#map-view',
      'ดูอาหารบนแผนที่สด',
      'แตะหมุดเพื่อเปิดการ์ดอาหารและดูจุดนัดรับได้ทันที',
    ],
    [
      home ? '#home-locate' : '#nearby-button',
      'หามื้อที่ใกล้คุณ',
      'อนุญาตตำแหน่งเมื่อพร้อม แล้วระบบจะเรียงอาหารตามระยะทางให้',
    ],
    [
      home ? '#home-food' : '#food-results',
      'เลือกและจองอาหาร',
      'แตะการ์ดเพื่ออ่านรายละเอียด จำนวนคงเหลือ และเวลานัดรับ',
    ],
    [
      '.mobile-nav a[href="/posts/new"], .header-post, .hero-buttons a[href="/posts/new"]',
      'ส่งต่ออาหารดี ๆ',
      'กดปุ่มบวกเพื่อปักหมุด ถ่ายรูป และแบ่งปันอาหาร',
    ],
  ];

  const get = (selector) => doc.querySelector(selector);
  const shade = get('.guide-shade');

  let index = 0;
  let target;
  let closed = false;

  const updateSpotlight = () => {
    if (!shade || closed) return;

    const rect = target?.getBoundingClientRect();
    const visible = !!rect && rect.width > 0 && rect.height > 0;

    shade.classList.toggle('is-spotlight', visible);
    if (!visible) return;

    shade.style.setProperty('--guide-left', `${rect.left - 8}px`);
    shade.style.setProperty('--guide-top', `${rect.top - 8}px`);
    shade.style.setProperty('--guide-width', `${rect.width + 16}px`);
    shade.style.setProperty('--guide-height', `${rect.height + 16}px`);
  };

  const show = () => {
    target?.classList.remove('guide-target');

    const [selector, title, copy] = steps[index];

    target = [...doc.querySelectorAll(selector)].find(
      (element) =>
        doc.defaultView.getComputedStyle(element).display !== 'none' &&
        element.getClientRects().length
    );

    if (target) {
      target.scrollIntoView({
        behavior: 'smooth',
        block: 'center',
      });
      target.classList.add('guide-target');
    }

    updateSpotlight();

    get('#guide-title').textContent = title;
    get('#guide-copy').textContent = copy;
    get('#guide-progress').textContent = `${index + 1} / ${steps.length}`;
    get('#guide-back').hidden = index === 0;
    get('#guide-next').textContent =
      index === steps.length - 1 ? 'เสร็จสิ้น' : 'ถัดไป';
  };

  const close = () => {
    if (closed) return;
    closed = true;

    doc.defaultView.removeEventListener('resize', updateSpotlight);
    doc.defaultView.removeEventListener('scroll', updateSpotlight, true);

    shade?.classList.remove('is-spotlight');
    target?.classList.remove('guide-target');

    shell.close();
    shell.hidden = true;
    doc.documentElement.classList.remove('guide-open');

    if (signedIn && complete) {
      Promise.resolve()
        .then(complete)
        .then(() => {
          const state = get('meta[name="onboarding-needed"]');
          if (state) state.content = 'false';
        })
        .catch(() => {
          onError('บันทึกสถานะคำแนะนำไม่ได้ ครั้งต่อไปอาจแสดงอีกครั้ง');
        });
    }
  };

  get('#guide-skip').onclick = close;

  get('#guide-back').onclick = () => {
    if (index > 0) {
      index--;
      show();
    }
  };

  get('#guide-next').onclick = () => {
    if (++index === steps.length) {
      close();
    } else {
      show();
    }
  };

  shell.addEventListener('cancel', (event) => {
    event.preventDefault();
    close();
  });

  shell.hidden = false;
  shell.showModal();
  doc.documentElement.classList.add('guide-open');

  doc.defaultView.addEventListener('resize', updateSpotlight);
  doc.defaultView.addEventListener('scroll', updateSpotlight, true);

  show();
  get('#guide-next').focus();
}
