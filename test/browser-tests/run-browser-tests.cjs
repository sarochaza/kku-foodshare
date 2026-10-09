'use strict';
const fs = require('node:fs');
const path = require('node:path');
const {spawn} = require('node:child_process');

const CASES = [
  ['about-image-check','source'],
  ['admin-posts-check','fixture'],
  ['dark-surfaces-check','fixture'],
  ['onboarding-check','fixture'],
  ['phase10-ui-check','fixture'],
  ['pickup-reminder-check','fixture'],
  ['browser-journey','live'],
  ['quick-actions-journey','live'],
  ['home-overflow-check','live']
].map(([name,scope])=>({name,scope,file:'browser-tests/'+name+'.cjs'}));

function runChild({script,projectRoot,directory,baseUrl,timeoutMs,quiet}) {
  return new Promise(resolve=>{
    const logFile=path.join(directory,'logs',script.name+'.log');
    const shots=path.join(directory,'screenshots',script.name);
    const reports=path.join(directory,'reports',script.name);
    fs.mkdirSync(shots,{recursive:true}); fs.mkdirSync(reports,{recursive:true});
    const log=fs.createWriteStream(logFile);
    const startedAt=new Date().toISOString();
    let error=null, timedOut=false;
    const child=spawn(process.execPath,[path.join(projectRoot,'test',script.file)],{
      cwd:projectRoot,
      env:{...process.env,
        TEST_BASE_URL:baseUrl,
        NODE_PATH:[path.join(projectRoot,'test','node_modules'),process.env.NODE_PATH].filter(Boolean).join(path.delimiter),
        PREVIEW_DIR:shots,BROWSER_SCREENSHOT_DIR:shots,BROWSER_REPORT_DIR:reports},
      windowsHide:true
    });
    const record=chunk=>{log.write(chunk);if(!quiet)process.stdout.write(chunk);};
    child.stdout.on('data',record); child.stderr.on('data',record);
    child.on('error',e=>{error=e.message;record(Buffer.from(e.message+'\n'));});
    const timer=setTimeout(()=>{
      timedOut=true; error='Test timeout after '+timeoutMs+'ms';
      record(Buffer.from(error+'\n')); child.kill('SIGKILL');
    },timeoutMs);
    child.on('close',(code,signal)=>{
      clearTimeout(timer);
      log.end(()=>{
        resolve({name:script.name,scope:script.scope,
          status:!timedOut&&!error&&code===0?'PASS':'FAIL',
          exitCode:code,signal,error,startedAt,finishedAt:new Date().toISOString(),
          log:'logs/'+script.name+'.log',screenshots:'screenshots/'+script.name,
          report:'reports/'+script.name});
      });
    });
  });
}

async function runSuite({projectRoot,scripts,omitted=[],baseUrl='http://localhost:8081',
    outputRoot,timeoutMs=300000,quiet=false}) {
  if(!scripts.length)throw Error('No test scripts selected');
  const parent=outputRoot||path.join(projectRoot,'test','reports','browser');
  fs.mkdirSync(parent,{recursive:true});
  const directory=fs.mkdtempSync(path.join(parent,new Date().toISOString().replace(/[:.]/g,'-')+'-'));
  for(const folder of ['logs','screenshots','reports'])fs.mkdirSync(path.join(directory,folder));
  const results=[];
  const save=()=>{
    const counts={pass:results.filter(r=>r.status==='PASS').length,
      fail:results.filter(r=>r.status==='FAIL').length,
      notRun:results.filter(r=>r.status==='NOT_RUN').length};
    const summary={baseUrl,generatedAt:new Date().toISOString(),counts,results,
      note:'PASS means this script exited successfully; source/fixture checks do not prove live API behavior.'};
    fs.writeFileSync(path.join(directory,'summary.json'),JSON.stringify(summary,null,2)+'\n');
    const quote=v=>'"'+String(v??'').replace(/"/g,'""')+'"';
    fs.writeFileSync(path.join(directory,'summary.csv'),
      ['name,scope,status,exitCode,log,error',...results.map(r=>[r.name,r.scope,r.status,r.exitCode,r.log,r.error].map(quote).join(','))].join('\n')+'\n');
    const lines=['# ผลทดสอบ Browser','',
      'เวลาในชื่อโฟลเดอร์และ JSON ใช้ UTC / ISO','',
      'ผ่าน '+counts.pass+' | ไม่ผ่าน '+counts.fail+' | ไม่ได้รัน '+counts.notRun,'',
      '| ไฟล์ | ขอบเขต | ผล | Exit code | Log |',
      '|---|---|---|---|---|',
      ...results.map(r=>'| '+r.name+' | '+r.scope+' | '+r.status+' | '+(r.exitCode??'—')+' | '+(r.log?'[เปิด Log]('+r.log+')':'—')+' |'),
      '', 'ภาพหน้าจออยู่ใน screenshots/ และรายงานแต่ละไฟล์อยู่ใน reports/',
      'ไฟล์ที่ไม่ได้สร้างภาพหรือรายงานเพิ่มเติมจะมีโฟลเดอร์ว่าง',
      'PASS ของ fixture/source ไม่ได้ยืนยัน API บนเว็บจริง; เทสที่ไม่ได้เลือกเป็น NOT_RUN',''];
    fs.writeFileSync(path.join(directory,'summary.md'),lines.join('\n'));
    return counts;
  };
  for(const script of scripts){
    if(!quiet)console.log('\nRunning '+script.name+' ...');
    results.push(await runChild({script,projectRoot,directory,baseUrl,timeoutMs,quiet}));
    save();
  }
  for(const script of omitted)results.push({name:script.name,scope:script.scope,status:'NOT_RUN',exitCode:null,
    log:null,error:null,reason:'Not selected for this run'});
  const counts=save();
  return {directory,results,counts,exitCode:counts.fail?1:0};
}

function parseArgs(args) {
  let baseUrl='http://localhost:8081',includeLive=false,names=null;
  for(let i=0;i<args.length;i++){
    if(args[i]==='--include-live')includeLive=true;
    else if(args[i]==='--base-url')baseUrl=args[++i];
    else if(args[i]==='--scripts')names=(args[++i]||'').split(',').filter(Boolean).map(s=>s.replace(/\.cjs$/,''));
    else throw Error('Unknown option: '+args[i]);
  }
  const url=new URL(baseUrl);
  if(!['http:','https:'].includes(url.protocol))throw Error('Base URL must use http or https');
  if(names&&(!names.length||names.some(n=>!CASES.some(c=>c.name===n))))throw Error('Unknown or empty script selection');
  const requested=CASES.filter(c=>!names||names.includes(c.name));
  if(names&&!includeLive&&requested.some(c=>c.scope==='live'))
    throw Error('Live scripts require --include-live and an isolated test database');
  const scripts=requested.filter(c=>includeLive||c.scope!=='live');
  const omitted=CASES.filter(c=>!scripts.includes(c));
  return {scripts,omitted,baseUrl};
}

if(require.main===module){
  (async()=>{
    const options=parseArgs(process.argv.slice(2));
    if(options.scripts.some(c=>c.scope==='live'))
      console.log('Live tests create test accounts/posts. Use an isolated test database.');
    const result=await runSuite({projectRoot:path.resolve(__dirname,'../..'),...options});
    console.log('\nReports: '+result.directory);
    console.log('PASS '+result.counts.pass+' / FAIL '+result.counts.fail+' / NOT_RUN '+result.counts.notRun);
    process.exitCode=result.exitCode;
  })().catch(e=>{console.error(e);process.exitCode=1;});
}
module.exports={runSuite,parseArgs};

