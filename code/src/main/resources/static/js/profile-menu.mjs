export function initProfileMenu() {
  const button=document.getElementById('profile-menu-button');
  const menu=document.getElementById('profile-menu');
  if(!button || !menu) return;
  const close=(restoreFocus=false)=>{
    if(menu.hidden) return;
    menu.hidden=true;button.setAttribute('aria-expanded','false');
    if(restoreFocus) button.focus();
  };
  button.addEventListener('click',()=>{
    const open=menu.hidden;
    menu.hidden=!open;button.setAttribute('aria-expanded',String(open));
  });
  document.addEventListener('keydown',event=>{if(event.key==='Escape')close(true);});
  document.addEventListener('click',event=>{
    if(!menu.hidden && !menu.contains(event.target) && !button.contains(event.target)) close();
  });
}
