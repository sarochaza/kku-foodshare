import test from 'node:test';
import assert from 'node:assert/strict';
import {readFile} from 'node:fs/promises';
import {initAuthUI} from '../../main/resources/static/js/auth.mjs';
test('password toggle changes only visibility and keeps value and autofill', () => {
  const input={type:'password',value:'SamplePass123!',name:'password',autocomplete:'current-password'};
  const attrs={},button={dataset:{passwordToggle:'login-password'},addEventListener(_name,fn){this.click=fn;},setAttribute(name,value){attrs[name]=value;}};
  const doc={querySelectorAll:()=>[button],getElementById:()=>input};
  initAuthUI(doc);button.click();
  assert.equal(input.type,'text');assert.equal(input.value,'SamplePass123!');assert.equal(attrs['aria-pressed'],'true');
  button.click();assert.equal(input.type,'password');assert.equal(attrs['aria-pressed'],'false');
  assert.equal(input.name,'password');assert.equal(input.autocomplete,'current-password');
});
test('styled forms retain POST bindings, validation, errors and reset link', async () => {
  const login=await readFile(new URL('../../main/resources/templates/login.html',import.meta.url),'utf8');
  const register=await readFile(new URL('../../main/resources/templates/register.html',import.meta.url),'utf8');
  assert.match(login,/th:action="@\{\/login\}" method="post"/);assert.match(login,/name="email"/);assert.match(login,/autocomplete="current-password"/);
  assert.match(login,/href="\/forgot-password"/);assert.match(login,/param.resetSuccess/);
  assert.match(register,/th:object="\$\{registerRequest\}"/);
  for(const field of ['displayName','email','password'])assert.ok(register.includes(`th:field="*{${field}}"`) && register.includes(`th:errors="*{${field}}"`));
  assert.match(register,/minlength="8"/);assert.match(register,/maxlength="72"/);
  for(const html of [login,register])assert.match(html,/<button type="button" class="password-toggle"/);
});
test('auth animation respects reduced-motion and has 320px layout and touch targets', async () => {
  const css=await readFile(new URL('../../main/resources/static/css/auth-polish.css',import.meta.url),'utf8');
  assert.match(css,/@keyframes auth-mascot-float/);assert.match(css,/@media \(prefers-reduced-motion: reduce\)/);
  assert.match(css,/animation: none/);assert.match(css,/@media \(max-width: 360px\)/);assert.match(css,/min-height: 44px/);
});
