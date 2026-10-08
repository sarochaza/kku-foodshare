import { signedIn } from './ui.js';

// Refresh the existing bell while the website is visible. No private requests for guests.
export function initNotificationBadge() {
  const bell = document.querySelector('.notification-bell');
  if (!signedIn() || !bell) return;
  let pending = false, stopped = false;
  const refresh = async () => {
    if (pending || stopped || document.hidden) return;
    pending = true;
    const controller = new AbortController();
    const timeout = setTimeout(() => controller.abort(), 8000);
    try {
      const response = await fetch('/api/v1/me/notifications/unread', {
        credentials: 'same-origin', cache: 'no-store', signal: controller.signal,
      });
      if (response.status === 401 || response.status === 403) { stop(); return; }
      if (!response.ok) return;
      const { count } = await response.json();
      if (!Number.isSafeInteger(count) || count < 0) return;
      let badge = bell.querySelector('.notification-badge');
      if (count === 0) {
        badge?.remove(); bell.setAttribute('aria-label', 'การแจ้งเตือน');
      } else {
        if (!badge) {
          badge = document.createElement('span'); badge.className = 'notification-badge'; bell.append(badge);
        }
        badge.textContent = count > 99 ? '99+' : String(count);
        badge.setAttribute('aria-hidden', 'true');
        bell.setAttribute('aria-label', `การแจ้งเตือน ยังไม่ได้อ่าน ${count} รายการ`);
      }
    } catch {
      // Preserve the last badge during temporary network/provider failures.
    } finally {clearTimeout(timeout); pending = false;}
  };
  const onVisible = () => {if (!document.hidden) refresh();};
  const timer = setInterval(refresh, 60000);
  function stop() {
    stopped = true; clearInterval(timer);
    document.removeEventListener('visibilitychange', onVisible);
  }
  document.addEventListener('visibilitychange', onVisible);
  refresh(); return stop;
}
