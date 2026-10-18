const fs = require('fs');
const path = require('path');
const vm = require('vm');
const root = path.resolve(__dirname, '../src/main/resources/static');
const files = fs.readdirSync(root, { recursive: true }).filter(p => /\.(html|js)$/.test(p));
const results = [];
const missingLinks = [];
for (const rel of files) {
  const content = fs.readFileSync(path.join(root, rel), 'utf8');
  const scripts = rel.endsWith('.js') ? [{ text: content, line: 1 }] : [...content.matchAll(/<script\b([^>]*)>([\s\S]*?)<\/script>/gi)].filter(m => !/\bsrc\s*=/.test(m[1]) && m[2].trim()).map(m => ({ text: m[2], line: content.slice(0, m.index + m[0].indexOf('>') + 1).split('\n').length }));
  for (const s of scripts) {
    try {
      new vm.Script(s.text, { filename: rel, lineOffset: s.line - 1 });
      results.push({ file: rel, status: 'PASS' });
    } catch (e) {
      results.push({ file: rel, status: 'FAIL', error: e.stack.split('\n').slice(0, 6).join('\n') });
    }
  }
  if (rel.endsWith('.html')) {
    for (const m of content.matchAll(/(?:href|src)\s*=\s*["']([^"']+)["']/gi)) {
      const link = m[1].split(/[?#]/)[0];
      if (!link || /^(https?:|data:|mailto:|javascript:|\/\/)/.test(link)) continue;
      const target = link.startsWith('/') ? path.join(root, link) : path.resolve(path.dirname(path.join(root, rel)), link);
      if (!fs.existsSync(target) && path.relative(root, target).replaceAll('\\', '/') !== 'css/normalize.css') missingLinks.push({ file: rel, link });
    }
  }
}
const report = { scripts: results.length, passed: results.filter(r => r.status === 'PASS').length, failed: results.filter(r => r.status === 'FAIL'), missingLinks };

console.log(JSON.stringify(report, null, 2));

if (results.some(r => r.status !== "PASS") || missingLinks.length) process.exitCode = 1;
