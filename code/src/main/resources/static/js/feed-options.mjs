export function postPageSize(value, fallback = 12) {
  const size = Number(value);
  return [6, 12, 24].includes(size) ? size : fallback;
}
