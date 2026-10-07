(function () {
  const reduce = matchMedia('(prefers-reduced-motion: reduce)').matches;
  const ease = t => 1 - Math.pow(1 - t, 3);

  function parse(el) {
    const t = el.textContent.trim();
    const m = t.match(/-?\d[\d,]*(\.\d+)?/);
    if (!m) return null;
    return { t, m, val: parseFloat(m[0].replace(/,/g, '')), dec: m[1] ? m[1].length - 1 : 0, comma: m[0].includes(',') };
  }
  function fmt(p, v) {
    let s = v.toFixed(p.dec);
    if (p.comma) { const [a, b] = s.split('.'); s = Number(a).toLocaleString('en-US') + (b ? '.' + b : ''); }
    return s;
  }
  function countUp(el, delay) {
    const p = parse(el);
    if (!p || reduce) return;
    const pre = p.t.slice(0, p.m.index), post = p.t.slice(p.m.index + p.m[0].length);
    el.textContent = pre + fmt(p, 0) + post;
    setTimeout(() => {
      const start = performance.now(), dur = 1300;
      requestAnimationFrame(function tick(now) {
        const k = Math.min(1, (now - start) / dur);
        el.textContent = pre + fmt(p, p.val * ease(k)) + post;
        if (k < 1) requestAnimationFrame(tick); else el.textContent = p.t;
      });
    }, delay);
  }

  document.addEventListener('DOMContentLoaded', () => {
    // meter: share of received money already spent (read before counters reset the text)
    const meter = document.querySelector('.meter');
    if (meter) {
      const get = r => { const e = document.querySelector('[data-role=' + r + ']'); const p = e && parse(e); return p ? p.val : 0; };
      const c = get('collected'), s = get('spent');
      const pct = c > 0 ? Math.min(100, s / c * 100) : (s > 0 ? 100 : 0);
      meter.classList.toggle('over', s > c);
      const label = document.getElementById('meter-label');
      if (label) label.textContent = Math.round(pct) + '% of the money received has been spent';
      setTimeout(() => { meter.firstElementChild.style.width = pct + '%'; }, 300);
    }

    // numbers count up from zero
    document.querySelectorAll('[data-count]').forEach((el, i) => countUp(el, 150 + i * 90));

    // one staggered entrance for the page's main blocks
    if (!reduce) {
      const sel = 'main .stat, main .meter-wrap, main tbody tr, main .friend-card, main .people, main .panel, main .day, main .flag, main .manage';
      [...document.querySelectorAll(sel)]
        .filter(el => !el.parentElement.closest(sel))
        .forEach((el, i) => { el.style.setProperty('--i', Math.min(i, 14)); el.classList.add('rv'); });
    }

    // open the picker when clicking anywhere on a date/month field
    document.querySelectorAll('input[type=date],input[type=month]').forEach(i =>
      i.addEventListener('click', () => { try { i.showPicker(); } catch (e) {} }));
  });
})();
