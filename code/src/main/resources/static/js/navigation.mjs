// Keep menu state independent from food filters and reservation logic.
export function navigationKey(pathname, search = '', hash = '', page = '') {
  const path = pathname.replace(/\/+$/, '') || '/';
  if (path === '/') return hash === '#how' ? 'how' : 'home';
  if (path === '/explore' || path === '/home')
    return new URLSearchParams(search).get('view') === 'map' ? 'map' : 'explore';
  if (path === '/posts/new' || page === 'editor') return 'editor';
  if (path === '/reservations') return 'reservations';
  if (path === '/account' || path.startsWith('/account/')) return 'account';
  return '';
}

export function applyNavigation(links, key) {
  for (const link of links) {
    // Mobile search covers both search views; desktop keeps them separate.
    const active = link.dataset.nav === key ||
      (key === 'map' && link.dataset.nav === 'explore' && link.closest('.mobile-nav')) ||
      (key === 'how' && link.dataset.nav === 'home' && link.closest('.mobile-nav'));
    link.classList.toggle('active', Boolean(active));
    if (active) link.setAttribute('aria-current', key === 'how' ? 'location' : 'page');
    else link.removeAttribute('aria-current');
  }
}

export function initNavigation(doc = document, win = window) {
  const links = [...doc.querySelectorAll('[data-nav]')];
  const sync = () => applyNavigation(links,
    navigationKey(win.location.pathname, win.location.search, win.location.hash, doc.body.dataset.page));
  sync();
  win.addEventListener('hashchange', sync);
  win.addEventListener('popstate', sync);
  win.addEventListener('foodshare:viewchange', event =>
    applyNavigation(links, event.detail.map ? 'map' : 'explore'));
}
