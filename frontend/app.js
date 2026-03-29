
const API_BASE = 'http://localhost:8080';

// ── Auth ──────────────────────────────────────────────────────
function encodeBasicAuth(u, p)  { return 'Basic ' + btoa(`${u}:${p}`); }
function getAuthHeader()        { return sessionStorage.getItem('gf_auth'); }
function getStoredUsername()    { return sessionStorage.getItem('gf_user') || ''; }

function saveSession(u, p) {
  sessionStorage.setItem('gf_auth', encodeBasicAuth(u, p));
  sessionStorage.setItem('gf_user', u);
}
function clearSession() {
  sessionStorage.removeItem('gf_auth');
  sessionStorage.removeItem('gf_user');
}

async function apiFetch(path, options = {}) {
  const auth = getAuthHeader();
  if (!auth) throw new Error('Не авторизовано');

  const res = await fetch(`${API_BASE}${path}`, {
    ...options,
    headers: {
      'Content-Type': 'application/json',
      'Authorization': auth,
      ...(options.headers || {}),
    },
  });

  if (!res.ok) {
    const err = new Error('Помилка сервера');
    err.status = res.status;
    throw err;
  }
  const text = await res.text();
  if (!text) return null;
  try { return JSON.parse(text); } catch (_) { return text; } // handle plain-text responses
}


function showLoginScreen() {
  document.getElementById('loginScreen').style.display = 'flex';
  document.getElementById('appShell').style.display   = 'none';
}
function showAppShell() {
  document.getElementById('loginScreen').style.display = 'none';
  document.getElementById('appShell').style.display    = 'block';
  document.getElementById('usernameDisplay').textContent = getStoredUsername();
  navigateTo('home');
}
function setLoginLoading(on) {
  document.getElementById('loginBtnText').classList.toggle('d-none', on);
  document.getElementById('loginSpinner').classList.toggle('d-none', !on);
  document.getElementById('btnLogin').disabled = on;
}
function showLoginError(msg) {
  document.getElementById('loginErrorMsg').textContent = msg;
  document.getElementById('loginError').style.display = 'block';
}
function hideLoginError() { document.getElementById('loginError').style.display = 'none'; }

function escapeHtml(s) {
  if (!s) return '';
  return String(s).replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;')
                  .replace(/"/g,'&quot;').replace(/'/g,'&#39;');
}

// ── Toast notifications (replaces alert() calls) ──────────────────────────
function showToast(message, type = 'danger') {
  // Create container if it doesn't exist
  let container = document.getElementById('toastContainer');
  if (!container) {
    container = document.createElement('div');
    container.id = 'toastContainer';
    container.style.cssText = 'position:fixed;bottom:1.5rem;right:1.5rem;z-index:9999;display:flex;flex-direction:column;gap:.5rem;';
    document.body.appendChild(container);
  }

  const icons = { danger: 'bi-exclamation-circle-fill', warning: 'bi-exclamation-triangle-fill', success: 'bi-check-circle-fill' };
  const colors = { danger: '#c0392b', warning: '#c8873a', success: '#27ae60' };

  const toast = document.createElement('div');
  toast.style.cssText = `
    background:#fff; border-left:4px solid ${colors[type] || colors.danger};
    border-radius:4px; box-shadow:0 4px 16px rgba(0,0,0,.15);
    padding:.75rem 1rem; font-size:.88rem; max-width:320px;
    display:flex; align-items:flex-start; gap:.6rem;
    animation:slideIn .2s ease;
  `;
  toast.innerHTML = `
    <i class="bi ${icons[type] || icons.danger}" style="color:${colors[type] || colors.danger};margin-top:1px;flex-shrink:0;"></i>
    <span>${escapeHtml(message)}</span>
  `;

  container.appendChild(toast);

  // Add keyframe animation once
  if (!document.getElementById('toastStyle')) {
    const style = document.createElement('style');
    style.id = 'toastStyle';
    style.textContent = '@keyframes slideIn{from{opacity:0;transform:translateX(20px)}to{opacity:1;transform:none}}';
    document.head.appendChild(style);
  }

  // Auto-remove after 4 seconds
  setTimeout(() => {
    toast.style.opacity = '0';
    toast.style.transition = 'opacity .3s';
    setTimeout(() => toast.remove(), 300);
  }, 4000);
}

function renderStars(rating) {
  if (!rating) return '<span class="text-muted small">Без оцінки</span>';
  return Array.from({length:5}, (_,i) =>
    `<i class="bi bi-star${i < rating ? '-fill' : ''} text-warning small"></i>`
  ).join('');
}

const PAGES = {
  home:            'pageHome',
  search:          'pageSearch',
  shelf:           'pageShelf',
  recommendations: 'pageRecommendations',
  community:       'pageCommunity',
  profile:         'pageProfile',
};

function navigateTo(key) {
  Object.values(PAGES).forEach(id => document.getElementById(id).classList.remove('active'));
  document.querySelectorAll('.nav-link[data-page]').forEach(l =>
    l.classList.toggle('active', l.dataset.page === key)
  );
  const el = document.getElementById(PAGES[key]);
  if (el) { el.classList.add('active'); onPageLoad(key); }
}

function onPageLoad(key) {
  if (key === 'home')            {} // static
  if (key === 'shelf')           loadShelf();
  if (key === 'community')       loadCommunity();
  if (key === 'profile')         loadProfile();
  if (key === 'recommendations') resetRecsUI();
}


async function attemptLogin(username, password) {
  setLoginLoading(true);
  hideLoginError();
  saveSession(username, password);
  try {
    const res = await fetch(`${API_BASE}/api/books/search?query=test`, {
      headers: { 'Authorization': getAuthHeader() }
    });
    if (res.status === 401) throw Object.assign(new Error(), { status: 401 });
    if (!res.ok)            throw new Error(`HTTP ${res.status}`);
    showAppShell();
  } catch (err) {
    clearSession();
    if (err.status === 401)
      showLoginError('Невірне ім\'я користувача або пароль.');
    else if (err.message.includes('Failed to fetch'))
      showLoginError('Не вдається підключитися до сервера. Чи запущено бекенд на порту 8080?');
    else
      showLoginError('Щось пішло не так. Спробуйте ще раз.');
  } finally { setLoginLoading(false); }
}

async function searchBooks() {
  const q = document.getElementById('searchInput').value.trim();
  if (!q) return;
  const c = document.getElementById('searchResults');
  c.innerHTML = `<div class="col-12 text-center py-5"><div class="spinner-border"></div></div>`;
  try {
    const books = await apiFetch(`/api/books/search?query=${encodeURIComponent(q)}`);
    renderBookGrid(books, c, true);
  } catch (err) {
    c.innerHTML = '<div class="col-12"><div class="alert alert-danger">Не вдалося виконати пошук. Спробуйте ще раз.</div></div>';
  }
}

function renderBookGrid(books, container, showAddBtn) {
  const list = Array.isArray(books) ? books : [];
  if (!list.length) {
    container.innerHTML = `<div class="col-12 text-muted">Книги не знайдено.</div>`;
    return;
  }
  container.innerHTML = list.map(b => `
    <div class="col">
      <div class="card h-100 shadow-sm book-card" onclick="openBookDetail('${escapeHtml(b.googleId)}')">
        <img src="${b.thumbnailUrl || 'https://placehold.co/128x192?text=Немає+обкладинки'}"
             class="card-img-top book-thumb" alt="${escapeHtml(b.title)}"
             onerror="this.src='https://placehold.co/128x192?text=Немає+обкладинки'" />
        <div class="card-body d-flex flex-column">
          <h6 class="card-title mb-1">${escapeHtml(b.title)}</h6>
          <p class="card-text text-muted small mb-3">${escapeHtml(b.author || '')}</p>
          ${showAddBtn ? `
          <button class="btn btn-add mt-auto"
            onclick="event.stopPropagation(); openAddModal('${escapeHtml(b.googleId)}', '${escapeHtml(b.title)}')">
            <i class="bi bi-plus-circle me-1"></i>Додати на полицю
          </button>` : ''}
        </div>
      </div>
    </div>`).join('');
}

async function openBookDetail(googleId) {
  const body   = document.getElementById('bookDetailBody');
  const addBtn = document.getElementById('btnDetailAddToShelf');
  body.innerHTML       = `<div class="text-center py-4"><div class="spinner-border"></div></div>`;
  addBtn.style.display = 'none';

  bootstrap.Modal.getOrCreateInstance(document.getElementById('bookDetailModal')).show();

  try {
    const b = await apiFetch(`/api/books/${encodeURIComponent(googleId)}`);
    renderBookDetail(b);
    addBtn.style.display = 'inline-block';
    addBtn.onclick = () => {
      bootstrap.Modal.getInstance(document.getElementById('bookDetailModal')).hide();
      openAddModal(b.googleId, b.title);
    };
  } catch (err) {
    body.innerHTML = '<div class="alert alert-danger">Не вдалося завантажити інформацію про книгу. Спробуйте ще раз.</div>';
  }
}

function renderBookDetail(b) {
  const stars = b.externalRating
    ? `<span class="text-warning me-1">★</span><strong>${b.externalRating.toFixed(1)}</strong> <span class="text-muted small">/ 10</span>`
    : '';
  const categories = (b.categories || [])
    .map(c => `<span class="book-meta-badge">${escapeHtml(c)}</span>`).join('');
  const imgSrc = b.thumbnailUrl || 'https://placehold.co/140x210?text=Немає+обкладинки';

  document.getElementById('bookDetailBody').innerHTML = `
    <div class="d-flex gap-4 flex-wrap">
      <img src="${imgSrc}" class="book-detail-cover"
           onerror="this.src='https://placehold.co/140x210?text=Немає+обкладинки'"
           alt="${escapeHtml(b.title)}" />
      <div class="flex-grow-1">
        <h4 class="mb-1" style="font-family:'Playfair Display',serif;">${escapeHtml(b.title)}</h4>
        <p class="text-muted mb-2">${escapeHtml(b.author || '')}</p>
        <div class="mb-2 d-flex flex-wrap gap-3 align-items-center">
          ${stars ? `<div class="rating-stars-lg">${stars}</div>` : ''}
          ${b.pageCount    ? `<span class="text-muted small"><i class="bi bi-file-text me-1"></i>${b.pageCount} стор.</span>` : ''}
          ${b.publishedDate? `<span class="text-muted small"><i class="bi bi-calendar3 me-1"></i>${b.publishedDate}</span>` : ''}
          ${b.isbn         ? `<span class="text-muted small"><i class="bi bi-upc me-1"></i>${b.isbn}</span>` : ''}
        </div>
        ${categories ? `<div class="mb-3">${categories}</div>` : ''}
        ${b.description
          ? `<p class="book-description">${escapeHtml(b.description)}</p>`
          : `<p class="text-muted small fst-italic">Опис відсутній.</p>`}
      </div>
    </div>`;
}

let _pendingGoogleId = null;

function openAddModal(googleId, title) {
  _pendingGoogleId = googleId;
  document.getElementById('modalBookTitle').textContent  = title;
  document.getElementById('addStatus').value             = 'CURRENTLY_READING';
  document.getElementById('addRating').value             = '0';
  document.getElementById('addModalError').style.display   = 'none';
  document.getElementById('addModalSuccess').style.display = 'none';
  const btn = document.getElementById('btnConfirmAdd');
  btn.disabled  = false;
  btn.innerHTML = '<i class="bi bi-check-lg me-1"></i>Додати на полицю';
  bootstrap.Modal.getOrCreateInstance(document.getElementById('addToShelfModal')).show();
}

async function confirmAddToShelf() {
  const googleId = _pendingGoogleId;
  const status   = document.getElementById('addStatus').value;
  const rating   = document.getElementById('addRating').value;
  const btn      = document.getElementById('btnConfirmAdd');
  btn.disabled   = true;
  btn.innerHTML  = '<span class="spinner-border spinner-border-sm me-1"></span>Додаємо…';
  document.getElementById('addModalError').style.display   = 'none';
  document.getElementById('addModalSuccess').style.display = 'none';

  try {
    await apiFetch(
      `/api/v1/shelves/${encodeURIComponent(googleId)}?status=${status}&rating=${rating}`,
      { method: 'POST' }
    );
    document.getElementById('addModalSuccess').style.display = 'block';
    btn.innerHTML = '<i class="bi bi-check-circle-fill me-1"></i>Додано!';
    setTimeout(() =>
      bootstrap.Modal.getInstance(document.getElementById('addToShelfModal')).hide(), 1200);
  } catch (err) {
    const el = document.getElementById('addModalError');
    el.textContent   = err.status === 409
      ? 'Ця книга вже є на вашій полиці.'
      : 'Не вдалося додати книгу. Спробуйте ще раз.';
    el.style.display = 'block';
    btn.disabled  = false;
    btn.innerHTML = '<i class="bi bi-check-lg me-1"></i>Додати на полицю';
  }
}

async function loadShelf() {
  const c = document.getElementById('shelfContent');
  c.innerHTML = `<div class="text-center py-5"><div class="spinner-border"></div></div>`;
  try {
    const books = await apiFetch('/api/v1/shelves?limit=100');
    renderShelf(books, c, true);
  } catch (err) {
    c.innerHTML = '<div class="alert alert-danger">Не вдалося завантажити полицю. Спробуйте оновити сторінку.</div>';
  }
}

const STATUS_LABEL = {
  CURRENTLY_READING: '📖 Читаю зараз',
  WANT_TO_READ:      '🔖 Хочу прочитати',
  FINISHED:          '✅ Прочитано',
  ABANDONED:         '🚫 Покинуто',
};
const STATUS_ORDER = ['CURRENTLY_READING', 'WANT_TO_READ', 'FINISHED', 'ABANDONED'];

function renderShelf(books, container, isOwn) {
  if (!books || !books.length) {
    container.innerHTML = `
      <div class="text-center text-muted py-5">
        <i class="bi bi-bookshelf fs-1 d-block mb-3"></i>
        ${isOwn
          ? 'Ваша полиця порожня. Знайдіть книгу та додайте її!'
          : 'Цей користувач ще не додав жодної книги.'}
      </div>`;
    return;
  }

  const groups = {};
  books.forEach(b => {
    const s = b.bookStatus || 'FINISHED';
    if (!groups[s]) groups[s] = [];
    groups[s].push(b);
  });

  const order = STATUS_ORDER.filter(s => groups[s])
    .concat(Object.keys(groups).filter(s => !STATUS_ORDER.includes(s)));

  container.innerHTML = order.map(status => `
    <div class="mb-5">
      <h5 class="shelf-group-title">${STATUS_LABEL[status] || status}</h5>
      <div class="row row-cols-1 row-cols-md-2 row-cols-lg-3 g-3">
        ${groups[status].map(b => shelfCard(b, isOwn)).join('')}
      </div>
    </div>`).join('');
}

function shelfCard(b, isOwn) {
  const stars  = renderStars(b.userRating || 0);
  const pct    = b.finishedPercentage > 0 ? `
    <div class="progress mt-2" style="height:4px;">
      <div class="progress-bar" style="width:${b.finishedPercentage}%;background:var(--amber)"></div>
    </div>
    <div class="text-muted small mt-1">${b.finishedPercentage}% прочитано</div>` : '';
  const review = b.review
    ? `<p class="fst-italic text-muted small mt-2 mb-0">"${escapeHtml(b.review)}"</p>` : '';
  const safeId    = escapeHtml(b.googleId  || '');
  const safeTitle = escapeHtml(b.bookTitle || '');

  // Book cover — we fetch the thumbnail by googleId asynchronously after render
  const coverId  = `cover-${safeId.replace(/[^a-zA-Z0-9]/g, '_')}`;
  const coverHtml = b.googleId
    ? `<div id="${coverId}" class="shelf-card-thumb-placeholder"><i class="bi bi-book"></i></div>`
    : `<div class="shelf-card-thumb-placeholder"><i class="bi bi-book"></i></div>`;

  // Schedule thumbnail fetch (non-blocking)
  if (b.googleId) {
    setTimeout(() => fetchShelfThumb(b.googleId, coverId), 0);
  }

  // Edit button — shown only for own shelf, floats top-right
  const editBtn = isOwn ? `
    <button class="shelf-edit-btn" title="Редагувати"
      onclick="event.stopPropagation();
               openUpdateModal('${safeId}','${safeTitle}',
                 ${b.userRating||0},'${b.bookStatus||'FINISHED'}',
                 ${b.finishedPercentage||0},'${escapeHtml(b.review||'')}')">
      <i class="bi bi-pencil"></i>
    </button>` : '';

  // Delete footer — shown only for own shelf
  const deleteFooter = isOwn ? `
    <div class="shelf-card-footer">
      <button class="btn btn-sm btn-outline-danger"
        onclick="deleteFromShelf('${safeId}','${safeTitle}',this)">
        <i class="bi bi-trash me-1"></i>Видалити
      </button>
    </div>` : '';

  return `
    <div class="col">
      <div class="card h-100 shadow-sm shelf-card">
        ${editBtn}
        ${coverHtml}
        <div class="shelf-card-body" onclick="openBookDetail('${safeId}')">
          <h6 class="shelf-card-title mb-1">${safeTitle}</h6>
          <div class="mb-1">${stars}</div>
          ${pct}${review}
        </div>
        ${deleteFooter}
      </div>
    </div>`;
}

async function fetchShelfThumb(googleId, placeholderId) {
  try {
    const b   = await apiFetch(`/api/books/${encodeURIComponent(googleId)}`);
    const el  = document.getElementById(placeholderId);
    if (!el) return;
    if (b.thumbnailUrl) {
      const img = document.createElement('img');
      img.src       = b.thumbnailUrl;
      img.alt       = b.title || '';
      img.className = 'shelf-card-thumb';
      img.onerror   = () => { /* keep placeholder look if img fails */ };
      el.replaceWith(img);
    }
  } catch (_) {
    // Silently ignore — placeholder stays
  }
}

async function deleteFromShelf(googleId, title, btn) {
  if (!confirm(`Видалити «${title}» з вашої полиці?`)) return;
  btn.disabled  = true;
  btn.innerHTML = '<span class="spinner-border spinner-border-sm"></span>';
  try {
    await apiFetch(`/api/v1/shelves/${encodeURIComponent(googleId)}`, { method: 'DELETE' });
    loadShelf();
  } catch (err) {
    showToast('Не вдалося видалити книгу. Спробуйте ще раз.', 'danger');
    btn.disabled  = false;
    btn.innerHTML = '<i class="bi bi-trash me-1"></i>Видалити';
  }
}

let _updateGoogleId = null;

function openUpdateModal(googleId, title, rating, status, pct, review) {
  _updateGoogleId = googleId;
  document.getElementById('updateModalTitle').textContent = title;
  document.getElementById('updateStatus').value = status || 'FINISHED';
  document.getElementById('updateRating').value = rating || 0;
  document.getElementById('updatePct').value    = pct    || 0;
  document.getElementById('updateReview').value = review || '';
  document.getElementById('updateModalError').style.display = 'none';
  const btn = document.getElementById('btnConfirmUpdate');
  btn.disabled  = false;
  btn.innerHTML = '<i class="bi bi-check-lg me-1"></i>Зберегти зміни';
  bootstrap.Modal.getOrCreateInstance(document.getElementById('updateShelfModal')).show();
}

async function confirmUpdate() {
  const btn    = document.getElementById('btnConfirmUpdate');
  const status = document.getElementById('updateStatus').value;
  const rating = document.getElementById('updateRating').value;
  const pct    = document.getElementById('updatePct').value;
  const review = document.getElementById('updateReview').value.trim();

  btn.disabled  = true;
  btn.innerHTML = '<span class="spinner-border spinner-border-sm me-1"></span>Зберігаємо…';
  document.getElementById('updateModalError').style.display = 'none';

  try {
    const params = new URLSearchParams({ status, rating });
    if (pct)    params.set('finishedPercentage', pct);
    if (review) params.set('review', review);

    await apiFetch(
      `/api/v1/shelves/${encodeURIComponent(_updateGoogleId)}/update?${params}`,
      { method: 'PATCH' }
    );
    bootstrap.Modal.getInstance(document.getElementById('updateShelfModal')).hide();
    loadShelf();
  } catch (err) {
    document.getElementById('updateModalError').textContent = 'Не вдалося зберегти зміни. Спробуйте ще раз.';
    document.getElementById('updateModalError').style.display = 'block';
    btn.disabled  = false;
    btn.innerHTML = '<i class="bi bi-check-lg me-1"></i>Зберегти зміни';
  }
}

function resetRecsUI() {
  document.getElementById('recsResults').innerHTML  = '';
  document.getElementById('recsLoading').style.display = 'none';
}

async function fetchRecommendations(customPrompt) {
  const loading = document.getElementById('recsLoading');
  const results = document.getElementById('recsResults');
  loading.style.display = 'block';
  results.innerHTML     = '';

  try {
    let data;
    if (customPrompt) {
      data = await apiFetch('/api/v1/recommendations/custom', {
        method: 'POST',
        body: JSON.stringify({ prompt: customPrompt }),
      });
    } else {
      data = await apiFetch('/api/v1/recommendations');
    }
    renderRecommendations(data);
  } catch (err) {
    results.innerHTML = '<div class="col-12"><div class="alert alert-danger">Не вдалося отримати рекомендації. Переконайтеся, що на вашій полиці є книги, та спробуйте ще раз.</div></div>';
  } finally {
    loading.style.display = 'none';
  }
}

function renderRecommendations(data) {
  const list    = data?.recommendations || [];
  const results = document.getElementById('recsResults');

  if (!list.length) {
    results.innerHTML = `<div class="col-12 text-muted">Рекомендації не знайдено. Спробуйте спочатку додати більше книг на полицю.</div>`;
    return;
  }

  results.innerHTML = list.map((r, i) => `
    <div class="col">
      <div class="rec-card shadow-sm">
        <div class="d-flex justify-content-between align-items-start mb-2">
          <span class="text-muted small">#${i + 1}</span>
          ${r.estimatedRating ? `<span class="rec-rating-badge">★ ${escapeHtml(r.estimatedRating)}</span>` : ''}
        </div>
        <h5 class="rec-card-title mb-1">${escapeHtml(r.title)}</h5>
        <p class="text-muted small mb-3">${escapeHtml(r.author || '')}</p>
        <p class="rec-reasoning mb-3">${escapeHtml(r.reasoning || '')}</p>
        <button class="btn btn-add btn-sm w-100"
          onclick="searchAndAdd('${escapeHtml(r.title)}', '${escapeHtml(r.author || '')}')">
          <i class="bi bi-plus-circle me-1"></i>Знайти та додати на полицю
        </button>
      </div>
    </div>`).join('');
}

async function searchAndAdd(title, author) {
  try {
    const books = await apiFetch(`/api/books/search?query=${encodeURIComponent(title + ' ' + author)}`);
    const list  = Array.isArray(books) ? books : [];
    const match = list.find(b => b.title.toLowerCase() === title.toLowerCase()) || list[0];
    if (match) openAddModal(match.googleId, match.title);
    else showToast('Книгу не знайдено в базі. Спробуйте пошук вручну.', 'warning');
  } catch (err) {
    showToast('Не вдалося виконати пошук. Спробуйте ще раз.', 'danger');
  }
}

async function loadCommunity() {
  const usersList = document.getElementById('usersList');
  const shelfDiv  = document.getElementById('communityShelfContent');
  usersList.innerHTML = `<div class="spinner-border spinner-border-sm"></div>`;
  shelfDiv.innerHTML  = '';

  try {
    const users = await apiFetch('/api/v1/users?limit=50');
    renderUsersList(users);
  } catch (err) {
    usersList.innerHTML = '<div class="alert alert-danger">Не вдалося завантажити список користувачів. Спробуйте ще раз.</div>';
  }
}

function renderUsersList(users) {
  const el = document.getElementById('usersList');
  if (!users || !users.length) {
    el.innerHTML = '<span class="text-muted small">Інших користувачів не знайдено.</span>';
    return;
  }
  const me = getStoredUsername();
  el.innerHTML = users.map(u => `
    <span class="user-pill ${u.username === me ? 'active' : ''}"
          onclick="loadUserShelf(${u.id}, '${escapeHtml(u.username)}', this)">
      <i class="bi bi-person"></i> ${escapeHtml(u.username)}
      ${u.username === me ? '<span style="font-size:.75rem;opacity:.6">(ви)</span>' : ''}
    </span>`).join('');
}

async function loadUserShelf(userId, username, pillEl) {
  document.querySelectorAll('.user-pill').forEach(p => p.classList.remove('active'));
  pillEl.classList.add('active');

  const c = document.getElementById('communityShelfContent');
  c.innerHTML = `
    <h5 class="shelf-group-title mb-3">
      <i class="bi bi-person me-1"></i>Полиця ${escapeHtml(username)}
    </h5>
    <div class="text-center py-4"><div class="spinner-border"></div></div>`;

  try {
    const books     = await apiFetch(`/api/v1/shelves/user/${userId}?limit=100`);
    const shelfBody = document.createElement('div');
    renderShelf(books, shelfBody, false);
    c.innerHTML = `
      <h5 class="shelf-group-title mb-3">
        <i class="bi bi-person me-1"></i>Полиця ${escapeHtml(username)}
        <span class="text-muted fw-normal" style="font-size:.85rem;font-family:'DM Sans',sans-serif;">
          — ${(books||[]).length} ${pluralBooks((books||[]).length)}
        </span>
      </h5>`;
    c.appendChild(shelfBody);
  } catch (err) {
    c.innerHTML += '<div class="alert alert-danger mt-2">Не вдалося завантажити полицю цього користувача. Спробуйте ще раз.</div>';
  }
}

function pluralBooks(n) {
  if (n % 10 === 1 && n % 100 !== 11) return 'книга';
  if ([2,3,4].includes(n % 10) && ![12,13,14].includes(n % 100)) return 'книги';
  return 'книг';
}


let _myProfile = null;

async function loadProfile() {
  // Reset feedback banners
  ['editProfileError','editProfileSuccess',
   'changePasswordError','changePasswordSuccess',
   'adminPasswordError','adminPasswordSuccess','adminDeleteError'].forEach(id => {
    const el = document.getElementById(id);
    if (el) el.style.display = 'none';
  });

  try {
    _myProfile = await apiFetch('/api/v1/users/me');
    renderProfileInfo(_myProfile);
    // Pre-fill edit fields
    document.getElementById('editUsername').value = _myProfile.username || '';
    document.getElementById('editEmail').value    = _myProfile.email    || '';
    // Check admin role and show panel
    checkAdminAndLoad(_myProfile);
  } catch (err) {
    document.getElementById('profileInfo').innerHTML =
'<div class="alert alert-danger">Не вдалося завантажити профіль. Спробуйте оновити сторінку.</div>';
  }
}

function renderProfileInfo(u) {
  const joined = u.createdDate
    ? new Date(u.createdDate).toLocaleDateString('uk-UA', { year:'numeric', month:'long', day:'numeric' })
    : '—';
  document.getElementById('profileInfo').innerHTML = `
    <h5 class="mb-0" style="color:#fff;font-family:'Playfair Display',serif;">${escapeHtml(u.username)}</h5>
    <p style="color:rgba(255,255,255,.55);font-size:.85rem;margin:.25rem 0 1.25rem;">${escapeHtml(u.email || '')}</p>
    <div class="d-flex justify-content-center gap-4">
      <div class="profile-stat"><strong>#${u.id}</strong>ID</div>
      <div class="profile-stat"><strong>${joined}</strong>Дата реєстрації</div>
    </div>`;
}

// ── Admin detection ───────────────────────────────────────────
// Uses the isAdmin field on UserResponse (added in recent backend update).
async function checkAdminAndLoad(myUser) {
  if (!myUser.isAdmin) {
    document.getElementById('adminPanel').style.display = 'none';
    return;
  }
  try {
    const users = await apiFetch('/api/v1/users?limit=100');
    showAdminPanel(users, myUser);
  } catch (err) {
    document.getElementById('adminPanel').style.display = 'none';
  }
}

function showAdminPanel(users, myUser) {
  document.getElementById('adminPanel').style.display = 'block';

  // Populate both user dropdowns, excluding self from delete
  const passwordSelect = document.getElementById('adminUserSelect');
  const deleteSelect   = document.getElementById('adminDeleteUserSelect');

  const options = users
    .map(u => `<option value="${u.id}">${escapeHtml(u.username)} (${escapeHtml(u.email || u.id)})</option>`)
    .join('');

  const optionsExcludingSelf = users
    .filter(u => u.id !== myUser.id)
    .map(u => `<option value="${u.id}">${escapeHtml(u.username)} (${escapeHtml(u.email || u.id)})</option>`)
    .join('');

  passwordSelect.innerHTML = '<option value="">— Оберіть користувача —</option>' + options;
  deleteSelect.innerHTML   = '<option value="">— Оберіть користувача —</option>' + optionsExcludingSelf;
}

async function saveProfile() {
  const username = document.getElementById('editUsername').value.trim();
  const email    = document.getElementById('editEmail').value.trim();
  const btn      = document.getElementById('btnSaveProfile');
  const errEl    = document.getElementById('editProfileError');
  const okEl     = document.getElementById('editProfileSuccess');

  errEl.style.display = 'none';
  okEl.style.display  = 'none';

  if (!username || !email) {
    errEl.textContent   = 'Ім\'я користувача та email є обов\'язковими.';
    errEl.style.display = 'block';
    return;
  }

  btn.disabled  = true;
  btn.innerHTML = '<span class="spinner-border spinner-border-sm me-1"></span>Зберігаємо…';

  try {
    const updated = await apiFetch('/api/v1/users/me', {
      method: 'PUT',
      body: JSON.stringify({ username, email }),
    });
    _myProfile = updated;
    renderProfileInfo(updated);
    // Update session username if it changed
    if (updated.username) {
      sessionStorage.setItem('gf_user', updated.username);
      document.getElementById('usernameDisplay').textContent = updated.username;
    }
    okEl.style.display = 'block';
  } catch (err) {
    errEl.textContent   = 'Не вдалося зберегти зміни. Спробуйте ще раз.';
    errEl.style.display = 'block';
  } finally {
    btn.disabled  = false;
    btn.innerHTML = '<i class="bi bi-check-lg me-1"></i> Зберегти зміни';
  }
}

async function changeOwnPassword() {
  const oldPw  = document.getElementById('oldPassword').value;
  const newPw  = document.getElementById('newPassword').value;
  const confPw = document.getElementById('confirmPassword').value;
  const btn    = document.getElementById('btnChangePassword');
  const errEl  = document.getElementById('changePasswordError');
  const okEl   = document.getElementById('changePasswordSuccess');

  errEl.style.display = 'none';
  okEl.style.display  = 'none';

  if (!oldPw || !newPw || !confPw) {
    errEl.textContent   = 'Будь ласка, заповніть усі поля.';
    errEl.style.display = 'block'; return;
  }
  if (newPw !== confPw) {
    errEl.textContent   = 'Новий пароль та підтвердження не збігаються.';
    errEl.style.display = 'block'; return;
  }
  if (newPw.length < 8) {
    errEl.textContent   = 'Новий пароль має містити щонайменше 8 символів.';
    errEl.style.display = 'block'; return;
  }

  btn.disabled  = true;
  btn.innerHTML = '<span class="spinner-border spinner-border-sm me-1"></span>Змінюємо…';

  try {
    await apiFetch('/api/v1/users/me/password', {
      method: 'PATCH',
      body: JSON.stringify({ oldPassword: oldPw, newPassword: newPw }),
    });
    okEl.style.display = 'block';
    // Update stored credentials so subsequent requests still work
    saveSession(_myProfile.username, newPw);
    document.getElementById('oldPassword').value     = '';
    document.getElementById('newPassword').value     = '';
    document.getElementById('confirmPassword').value = '';
  } catch (err) {
    errEl.textContent = err.status === 400
      ? 'Поточний пароль введено невірно.'
      : 'Не вдалося змінити пароль. Спробуйте ще раз.';
    errEl.style.display = 'block';
  } finally {
    btn.disabled  = false;
    btn.innerHTML = '<i class="bi bi-shield-check me-1"></i> Змінити пароль';
  }
}

async function adminChangePassword() {
  const userId  = document.getElementById('adminUserSelect').value;
  const newPw   = document.getElementById('adminNewPassword').value;
  const confPw  = document.getElementById('adminConfirmPassword').value;
  const oldPw  = "DummyPass@@2";
  const btn     = document.getElementById('btnAdminChangePassword');
  const errEl   = document.getElementById('adminPasswordError');
  const okEl    = document.getElementById('adminPasswordSuccess');

  errEl.style.display = 'none';
  okEl.style.display  = 'none';

  if (!userId) {
    errEl.textContent   = 'Оберіть користувача.';
    errEl.style.display = 'block'; return;
  }
  if (!newPw || !confPw) {
    errEl.textContent   = 'Введіть і підтвердьте новий пароль.';
    errEl.style.display = 'block'; return;
  }
  if (newPw !== confPw) {
    errEl.textContent   = 'Паролі не збігаються.';
    errEl.style.display = 'block'; return;
  }
  if (newPw.length < 8) {
    errEl.textContent   = 'Пароль має містити щонайменше 8 символів.';
    errEl.style.display = 'block'; return;
  }

  btn.disabled  = true;
  btn.innerHTML = '<span class="spinner-border spinner-border-sm me-1"></span>Змінюємо…';

  try {
    // PATCH /api/v1/users/{userId}/password-admin — admin-only, only newPassword required
    await apiFetch(`/api/v1/users/${userId}/password-admin`, {
      method: 'PATCH',
      body: JSON.stringify({
        oldPassword: oldPw,
        newPassword: newPw }),
    });
    okEl.style.display = 'block';
    document.getElementById('adminNewPassword').value     = '';
    document.getElementById('adminConfirmPassword').value = '';
  } catch (err) {
    errEl.textContent   = 'Не вдалося зберегти зміни. Спробуйте ще раз.';
    errEl.style.display = 'block';
  } finally {
    btn.disabled  = false;
    btn.innerHTML = '<i class="bi bi-key me-1"></i> Змінити пароль користувача';
  }
}

let _deleteUserId   = null;
let _deleteUserName = null;

function adminDeleteUser() {
  const selEl = document.getElementById('adminDeleteUserSelect');
  const errEl = document.getElementById('adminDeleteError');
  errEl.style.display = 'none';

  const userId = selEl.value;
  if (!userId) {
    errEl.textContent   = 'Оберіть користувача для видалення.';
    errEl.style.display = 'block'; return;
  }

  _deleteUserId   = userId;
  _deleteUserName = selEl.options[selEl.selectedIndex].text;
  document.getElementById('confirmDeleteUsername').textContent = _deleteUserName;
  bootstrap.Modal.getOrCreateInstance(
    document.getElementById('confirmDeleteModal')
  ).show();
}

async function executeDeleteUser() {
  const errEl = document.getElementById('adminDeleteError');
  const confirmBtn = document.getElementById('btnConfirmDeleteUser');

  bootstrap.Modal.getInstance(
    document.getElementById('confirmDeleteModal')
  ).hide();

  const deleteBtn = document.getElementById('btnAdminDeleteUser');
  deleteBtn.disabled  = true;
  deleteBtn.innerHTML = '<span class="spinner-border spinner-border-sm me-1"></span>Видаляємо…';
  errEl.style.display = 'none';

  try {
    await apiFetch(`/api/v1/users/${_deleteUserId}`, { method: 'DELETE' });
    _deleteUserId   = null;
    _deleteUserName = null;
    deleteBtn.disabled  = false;
    deleteBtn.innerHTML = '<i class="bi bi-person-x me-1"></i> Видалити користувача';
    loadProfile(); // refresh user lists
  } catch (err) {
    errEl.textContent   = 'Не вдалося зберегти зміни. Спробуйте ще раз.';
    errEl.style.display = 'block';
    deleteBtn.disabled  = false;
    deleteBtn.innerHTML = '<i class="bi bi-person-x me-1"></i> Видалити користувача';
  }
}

const PASSWORD_REGEX = /^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&*()\_+\-=\[\]{};':"\\|,.<>\/?]).{8,32}$/;

function showRegisterCard() {
  document.getElementById('loginCard').style.display    = 'none';
  document.getElementById('registerCard').style.display = 'block';
  // Clear fields and feedback
  ['regUsername','regEmail','regPassword','regConfirmPassword'].forEach(id => {
    document.getElementById(id).value = '';
  });
  document.getElementById('registerError').style.display   = 'none';
  document.getElementById('registerSuccess').style.display = 'none';
}

function showLoginCard() {
  document.getElementById('registerCard').style.display = 'none';
  document.getElementById('loginCard').style.display    = 'block';
}

function setRegisterLoading(on) {
  document.getElementById('registerBtnText').classList.toggle('d-none', on);
  document.getElementById('registerSpinner').classList.toggle('d-none', !on);
  document.getElementById('btnRegister').disabled = on;
}

function showRegisterError(msg) {
  document.getElementById('registerErrorMsg').textContent = msg;
  document.getElementById('registerError').style.display  = 'block';
  document.getElementById('registerSuccess').style.display = 'none';
}

function showRegisterSuccess() {
  document.getElementById('registerError').style.display   = 'none';
  document.getElementById('registerSuccess').style.display = 'block';
  // Auto-flip back to login after 2s
  setTimeout(() => {
    showLoginCard();
    // Pre-fill username on login form for convenience
    const u = document.getElementById('regUsername').value.trim();
    if (u) document.getElementById('inputUsername').value = u;
  }, 2000);
}

async function attemptRegister() {
  const username = document.getElementById('regUsername').value.trim();
  const email    = document.getElementById('regEmail').value.trim();
  const password = document.getElementById('regPassword').value;
  const confirm  = document.getElementById('regConfirmPassword').value;

  document.getElementById('registerError').style.display   = 'none';
  document.getElementById('registerSuccess').style.display = 'none';

  // Client-side validation
  if (!username || !email || !password || !confirm) {
    showRegisterError('Будь ласка, заповніть усі поля.'); return;
  }
  if (password !== confirm) {
    showRegisterError('Паролі не збігаються.'); return;
  }
  if (!PASSWORD_REGEX.test(password)) {
    showRegisterError('Пароль не відповідає вимогам: 8–32 символи, велика та мала літера, цифра та спецсимвол.'); return;
  }

  setRegisterLoading(true);
  try {
    await fetch(`${API_BASE}/api/v1/users`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ username, email, password }),
    }).then(async res => {
      if (!res.ok) {
        const text = await res.text();
        let msg = `Помилка ${res.status}`;
        try {
          const json = JSON.parse(text);
          msg = json.message || json.error || msg;
        } catch (_) { msg = text || msg; }
        throw new Error(msg);
      }
    });
    showRegisterSuccess();
  } catch (err) {
    if (err.message.includes('Failed to fetch'))
      showRegisterError('Не вдається підключитися до сервера. Перевірте зʼєднання.');
    else
      showRegisterError('Не вдалося створити акаунт. Можливо, це імʼя користувача вже зайнято.');
  } finally {
    setRegisterLoading(false);
  }
}

document.addEventListener('DOMContentLoaded', () => {

  // Toggle login ↔ register
  document.getElementById('linkShowRegister').addEventListener('click', e => { e.preventDefault(); showRegisterCard(); });
  document.getElementById('linkShowLogin').addEventListener('click',    e => { e.preventDefault(); showLoginCard(); });

  // Register submit
  document.getElementById('btnRegister').addEventListener('click', attemptRegister);
  ['regUsername','regEmail','regPassword','regConfirmPassword'].forEach(id => {
    document.getElementById(id).addEventListener('keydown', e => {
      if (e.key === 'Enter') attemptRegister();
    });
  });

  // Login
  document.getElementById('btnLogin').addEventListener('click', () => {
    const u = document.getElementById('inputUsername').value.trim();
    const p = document.getElementById('inputPassword').value;
    if (!u || !p) { showLoginError('Будь ласка, введіть ім\'я користувача та пароль.'); return; }
    attemptLogin(u, p);
  });
  ['inputUsername','inputPassword'].forEach(id =>
    document.getElementById(id).addEventListener('keydown', e => {
      if (e.key === 'Enter') document.getElementById('btnLogin').click();
    })
  );

  // Logout
  document.getElementById('btnLogout').addEventListener('click', () => {
    clearSession(); showLoginScreen();
    document.getElementById('inputUsername').value = '';
    document.getElementById('inputPassword').value = '';
  });

  // Navbar
  document.querySelectorAll('.nav-link[data-page]').forEach(l =>
    l.addEventListener('click', e => { e.preventDefault(); navigateTo(l.dataset.page); })
  );

  // Search
  document.getElementById('btnSearch').addEventListener('click', searchBooks);
  document.getElementById('searchInput').addEventListener('keydown', e => {
    if (e.key === 'Enter') searchBooks();
  });

  // Add modal confirm
  document.getElementById('btnConfirmAdd').addEventListener('click', confirmAddToShelf);

  // Update modal confirm
  document.getElementById('btnConfirmUpdate').addEventListener('click', confirmUpdate);

  // Recommendations — standard
  document.getElementById('btnGetRecs').addEventListener('click', () => fetchRecommendations(null));

  // Recommendations — custom prompt
  document.getElementById('btnCustomRecs').addEventListener('click', () => {
    const prompt = document.getElementById('customPromptInput').value.trim();
    if (!prompt) { showToast('Будь ласка, введіть власний запит перед відправкою.', 'warning'); return; }
    fetchRecommendations(prompt);
  });

  // Clear prompt
  document.getElementById('btnClearPrompt').addEventListener('click', () => {
    document.getElementById('customPromptInput').value = '';
  });

  // Profile — save profile info
  document.getElementById('btnSaveProfile').addEventListener('click', saveProfile);

  // Profile — change own password
  document.getElementById('btnChangePassword').addEventListener('click', changeOwnPassword);

  // Admin — change user password
  document.getElementById('btnAdminChangePassword').addEventListener('click', adminChangePassword);

  // Admin — delete user (opens confirmation modal)
  document.getElementById('btnAdminDeleteUser').addEventListener('click', adminDeleteUser);
  // Admin — confirm delete inside modal
  document.getElementById('btnConfirmDeleteUser').addEventListener('click', executeDeleteUser);

  if (getAuthHeader()) showAppShell();
  else                 showLoginScreen();
});
