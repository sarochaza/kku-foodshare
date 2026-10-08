const THEME_STORAGE_KEY = 'kku-foodshare-theme';
const THEMES = new Set(['color', 'monochrome', 'dark']);

function browserStorage() {
  try {
    return globalThis.localStorage;
  } catch {
    return null;
  }
}

export function initThemeSelector(documentRef = globalThis.document, storage = browserStorage()) {
  if (!documentRef?.documentElement) return 'color';

  let theme = 'color';
  try {
    const savedTheme = storage?.getItem(THEME_STORAGE_KEY);
    theme = THEMES.has(savedTheme) ? savedTheme : 'color';
  } catch {
    theme = 'color';
  }

  const selectors = [...documentRef.querySelectorAll('[data-theme-select]')];
  const render = () => {
    documentRef.documentElement.dataset.theme = theme;
    for (const selector of selectors) selector.value = theme;
  };

  render();
  for (const selector of selectors) {
    selector.addEventListener('change', event => {
      const nextTheme = event.target?.value;
      if (!THEMES.has(nextTheme)) return;
      theme = nextTheme;
      render();
      try {
        storage?.setItem(THEME_STORAGE_KEY, theme);
      } catch {
        // The selected theme remains usable for this page if browser storage is unavailable.
      }
    });
  }

  return theme;
}

if (typeof document !== 'undefined') initThemeSelector(document);
