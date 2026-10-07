// Only presentation: keep native POST, validation, CSRF and autofill unchanged.
export function initAuthUI(doc = document) {
  doc.querySelectorAll('[data-password-toggle]').forEach(button => {
    if (button.dataset.initialized) return;
    button.dataset.initialized = 'true';
    button.addEventListener('click', () => {
      const input = doc.getElementById(button.dataset.passwordToggle);
      if (!input || !['password', 'text'].includes(input.type)) return;
      const visible = input.type === 'password';
      input.type = visible ? 'text' : 'password';
      button.textContent = visible ? 'ซ่อน' : 'แสดง';
      button.setAttribute('aria-pressed', String(visible));
      button.setAttribute('aria-label', visible ? 'ซ่อนรหัสผ่าน' : 'แสดงรหัสผ่าน');
    });
  });
}
