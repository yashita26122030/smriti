const fs = require('node:fs');
const path = require('node:path');
const { build } = require('esbuild');

const root = path.resolve(__dirname, '..');
const webDir = path.join(root, 'www');

async function main() {
  fs.mkdirSync(webDir, { recursive: true });
  fs.copyFileSync(path.join(root, 'index.html'), path.join(webDir, 'index.html'));
  fs.cpSync(path.join(root, 'assets'), path.join(webDir, 'assets'), { recursive: true });
  await build({
    entryPoints: [path.join(root, 'mobile-bridge.js')],
    outfile: path.join(webDir, 'mobile-bridge.js'),
    bundle: true,
    platform: 'browser',
    format: 'iife',
    target: ['es2020']
  });
}

main().catch(error => {
  console.error(error);
  process.exitCode = 1;
});
