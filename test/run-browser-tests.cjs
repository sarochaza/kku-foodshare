const {spawnSync}=require('node:child_process');
const path=require('node:path');
const result=spawnSync(process.execPath,[
  path.join(__dirname,'browser-tests','run-browser-tests.cjs'),
  ...process.argv.slice(2)
],{stdio:'inherit'});
if(result.error){console.error(result.error.message);process.exit(1);}
process.exit(result.status===null?1:result.status);