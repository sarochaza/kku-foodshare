'use strict';
const test = require('node:test');
const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const os = require('node:os');
const runnerPath = path.join(__dirname, 'run-browser-tests.cjs');

function loadRunner() {
  assert.ok(fs.existsSync(runnerPath), 'Browser report runner is missing');
  return require(runnerPath);
}
function fixture(t) {
  const root = fs.mkdtempSync(path.join(os.tmpdir(), 'foodshare reports '));
  fs.mkdirSync(path.join(root, 'test'));
  t.after(() => fs.rmSync(root, {recursive: true, force: true}));
  return root;
}
function script(root, name, source) {
  fs.writeFileSync(path.join(root, 'test', name + '.cjs'), source);
  return {name, file: name + '.cjs', scope: 'fixture'};
}

test('records real exit codes and stdout/stderr, keeps running after a failure and groups artifacts', async t => {
  const {runSuite} = loadRunner();
  const root = fixture(t);
  const first = script(root, 'first', `console.log('first stdout'); console.error('first stderr');
    require('node:fs').writeFileSync(require('node:path').join(process.env.BROWSER_SCREENSHOT_DIR,'sample.png'),'image fixture');
    require('node:fs').writeFileSync(require('node:path').join(process.env.BROWSER_REPORT_DIR,'details.json'),'{}');`);
  const broken = script(root, 'broken', "console.error('assertion failed'); process.exitCode=7;");
  const last = script(root, 'last', "console.log('last ran');");
  const result = await runSuite({projectRoot: root, scripts: [first, broken, last], quiet: true});
  assert.deepEqual(result.results.map(r => [r.status, r.exitCode]), [['PASS',0],['FAIL',7],['PASS',0]]);
  assert.deepEqual(result.counts, {pass:2, fail:1, notRun:0});
  assert.equal(result.exitCode, 1);
  const log = fs.readFileSync(path.join(result.directory, 'logs', 'first.log'), 'utf8');
  assert.match(log, /first stdout/); assert.match(log, /first stderr/);
  assert.ok(fs.existsSync(path.join(result.directory, 'screenshots', 'first', 'sample.png')));
  assert.ok(fs.existsSync(path.join(result.directory, 'reports', 'first', 'details.json')));
  assert.match(fs.readFileSync(path.join(result.directory,'logs','last.log'),'utf8'), /last ran/);
  assert.equal(JSON.parse(fs.readFileSync(path.join(result.directory,'summary.json'))).counts.fail, 1);
  assert.match(fs.readFileSync(path.join(result.directory,'summary.md'),'utf8'), /broken.*FAIL/);
});

test('creates distinct run directories and labels omitted tests NOT_RUN instead of PASS', async t => {
  const {runSuite} = loadRunner();
  const root = fixture(t);
  const selected = script(root, 'selected', "console.log('ok');");
  const omitted = script(root, 'omitted', "throw Error('must not run');");
  const options = {projectRoot:root, scripts:[selected], omitted:[omitted], quiet:true};
  const first=await runSuite(options), second=await runSuite(options);
  assert.notEqual(first.directory,second.directory);
  assert.deepEqual(second.counts,{pass:1,fail:0,notRun:1});
  assert.equal(second.results[1].status,'NOT_RUN');
  assert.equal(second.results[1].exitCode,null);
  assert.equal(second.exitCode,0);
});

test('a timed out child is a failure with a persisted report', async t => {
  const {runSuite} = loadRunner();
  const root = fixture(t);
  const stalled=script(root,'stalled',"setInterval(()=>{},1000);");
  const result=await runSuite({projectRoot:root,scripts:[stalled],timeoutMs:500,quiet:true});
  assert.equal(result.results[0].status,'FAIL');
  assert.match(result.results[0].error,/timeout/i);
  assert.equal(result.exitCode,1);
  assert.ok(fs.existsSync(path.join(result.directory,'summary.csv')));
});

