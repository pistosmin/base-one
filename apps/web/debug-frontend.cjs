const puppeteer = require('puppeteer');

(async () => {
  const browser = await puppeteer.launch({ headless: 'new' });
  const page = await browser.newPage();
  
  page.on('console', msg => {
    if (msg.type() === 'error') {
      console.log(`[Browser Error]:`, msg.text());
    } else if (msg.type() === 'warning') {
      console.log(`[Browser Warning]:`, msg.text());
    }
  });

  page.on('pageerror', error => {
    console.log(`[Browser PageError]:`, error.message);
  });

  console.log('Navigating to http://localhost:5173/login ...');
  try {
    await page.goto('http://localhost:5173/login', { waitUntil: 'networkidle0' });
    console.log('Navigation complete. Waiting for specific errors...');
    await new Promise(r => setTimeout(r, 2000));
  } catch (err) {
    console.error('Failed to load page:', err);
  } finally {
    await browser.close();
  }
})();
