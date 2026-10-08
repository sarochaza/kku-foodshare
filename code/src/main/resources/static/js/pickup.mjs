// The QR contains only the existing reservation identifier and pickup code.
// The server still checks ownership, reservation state, time and the code hash.
export function payload(id, code) {
  if (!Number.isSafeInteger(Number(id)) || Number(id) < 1 || !/^[0-9]{6}$/.test(code))
    throw new Error("ข้อมูลบัตรรับอาหารไม่ถูกต้อง");
  return `FS1:${id}:${code}`;
}

export function parsePass(value, expectedId = null) {
  const match = /^FS1:([1-9][0-9]*):([0-9]{6})$/.exec(value || "");
  const id = Number(match?.[1]);
  if (
    !match ||
    !Number.isSafeInteger(id) ||
    (expectedId != null && id !== Number(expectedId))
  )
    return null;
  return { id, code: match[2] };
}

export function drawPass(element, id, code) {
  if (!element || !window.FoodShareQR) return;
  window.FoodShareQR.render(element, payload(id, code));
}
