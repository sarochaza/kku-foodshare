export function safeImageUrl(value) {
  if (typeof value !== 'string' || !value.trim()) return '';
  const url = value.trim();
  try {
    const parsed = new URL(url, 'https://foodshare.local');
    return ['https:', 'http:'].includes(parsed.protocol) ? url : '';
  } catch { return ''; }
}

export function imageIndex(index, delta, total) {
  return total > 0 ? ((index + delta) % total + total) % total : 0;
}

export function initImageViewer(doc = document) {
  const dialog = doc.querySelector('#image-viewer');
  if (!dialog) return;
  const image = dialog.querySelector('[data-viewer-image]');
  const title = dialog.querySelector('[data-viewer-title]');
  const count = dialog.querySelector('[data-viewer-count]');
  const previous = dialog.querySelector('[data-viewer-previous]');
  const next = dialog.querySelector('[data-viewer-next]');
  const error = dialog.querySelector('[data-viewer-error]');
  const stage = dialog.querySelector('[data-viewer-stage]');
  let images = [], index = 0, opener = null, name = '', touch = null;

  const show = () => {
    image.hidden = false;
    error.hidden = true;
    image.alt = `${name || 'ภาพอาหาร'} รูปที่ ${index + 1}`;
    image.src = images[index];
    title.textContent = name || 'รูปอาหาร';
    count.textContent = `รูป ${index + 1} จาก ${images.length}`;
    previous.hidden = next.hidden = images.length < 2;
  };
  const move = delta => { index = imageIndex(index, delta, images.length); show(); };
  const close = () => { if (dialog.open) dialog.close(); };
  previous.addEventListener('click', () => move(-1));
  next.addEventListener('click', () => move(1));
  dialog.querySelector('[data-viewer-close]').addEventListener('click', close);
  dialog.addEventListener('click', event => { if (event.target === dialog) close(); });
  dialog.addEventListener('cancel', event => { event.preventDefault(); close(); });
  dialog.addEventListener('close', () => {
    image.removeAttribute('src');
    touch = null;
    if (opener?.isConnected) opener.focus({preventScroll: true});
  });
  dialog.addEventListener('keydown', event => {
    if (event.key === 'ArrowRight' || event.key === 'ArrowLeft') {
      event.preventDefault(); move(event.key === 'ArrowRight' ? 1 : -1);
    }
  });
  image.addEventListener('error', () => {
    image.hidden = true;
    error.hidden = false;
  });
  stage.addEventListener('touchstart', event => {
    touch = event.touches.length === 1 ? {x: event.touches[0].clientX, y: event.touches[0].clientY} : null;
  }, {passive: true});
  stage.addEventListener('touchend', event => {
    if (!touch || event.touches.length || !event.changedTouches.length) return;
    const dx = event.changedTouches[0].clientX - touch.x;
    const dy = event.changedTouches[0].clientY - touch.y;
    touch = null;
    if (images.length > 1 && Math.abs(dx) > 50 && Math.abs(dx) > Math.abs(dy) * 1.5) move(dx < 0 ? 1 : -1);
  }, {passive: true});

  // Delegation supports newly loaded posts and pagination without extra bindings.
  doc.addEventListener('click', event => {
    const link = event.target.closest?.('[data-gallery-image]');
    if (!link || event.defaultPrevented || event.ctrlKey || event.metaKey || event.shiftKey || event.altKey || event.button > 0) return;
    const gallery = link.closest('[data-gallery-images]');
    if (!gallery || typeof dialog.showModal !== 'function') return;
    let urls;
    try { urls = JSON.parse(gallery.dataset.galleryImages); } catch { return; }
    if (!Array.isArray(urls) || !urls.length || urls.some(url => !safeImageUrl(url))) return;
    images = urls;
    index = Math.min(Math.max(0, Number(link.dataset.galleryImage) || 0), images.length - 1);
    name = gallery.dataset.galleryTitle || '';
    opener = link;
    touch = null;
    show();
    dialog.showModal();
    event.preventDefault();
  });
}
