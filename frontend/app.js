/* ═══════════════════════════════════════════════════════════
   GoodFiction — app.js
   Step 3: Search, Add to Shelf, My Shelf
   ═══════════════════════════════════════════════════════════ */

const API_BASE = 'http://localhost:8080';

// ── Auth helpers ──────────────────────────────────────────────

function encodeBasicAuth(username, password) {
  return 'Basic ' + btoa(`${username}:${password}`);
}
function getAuthHeader()       { return sessionStorage.getItem('gf_auth'); }
function getStoredUsername()   { return sessionStorage.getItem('gf_user') || ''; }

function saveSession(username, password) {
  sessionStorage.setItem('gf_auth', encodeBasicAuth(username, password));
  sessionStorage.setItem('gf_user', username);
}
function clearSession() {
  sessionStorage.removeItem('gf_auth');
  sessionStorage.removeItem('gf_user');
}

// ── Central fetch wrapper ─────────────────────────────────────

async function apiFetch(path, options = {}) {
  const authHeader = getAuthHeader();
  if (!authHeader) throw new Error('Not authenticated');

  const response = await fetch(`${API_BASE}${path}`, {
    ...options,
    headers: {
      'Content-Type': 'application/json',
      'Authorization': authHeader,
      ...(options.headers || {}),
    },
  });

  if (!response.ok) {
    const err = new Error(`HTTP ${response.status}: ${response.statusText}`);
    err.status = response.status;
    throw err;
  }

  const text = await response.text();
  return text ? JSON.parse(text) : null;
}

// ── UI: Login / App shell ─────────────────────────────────────

function showLoginScreen() {
  document.getElementById('loginScreen').style.display = 'flex';
  document.getElementById('appShell').style.display   = 'none';
}

function showAppShell() {
  document.getElementById('loginScreen').style.display = 'none';
  document.getElementById('appShell').style.display    = 'block';
  document.getElementById('usernameDisplay').textContent = getStoredUsername();
  navigateTo('search');
}

function setLoginLoading(isLoading) {
  document.getElementById('loginBtnText').classList.toggle('d-none', isLoading);
  document.getElementById('loginSpinner').classList.toggle('d-none', !isLoading);
  document.getElementById('btnLogin').disabled = isLoading;
}

function showLoginError(msg) {
  const el = document.getElementById('loginError');
  document.getElementById('loginErrorMsg').textContent = msg;
  el.style.display = 'block';
}
function hideLoginError() {
  document.getElementById('loginError').style.display = 'none';
}

// ── Navigation ────────────────────────────────────────────────

const PAGES = {
  search:          'pageSearch',
  shelf:           'pageShelf',
  recommendations: 'pageRecommendations',
};

function navigateTo(pageKey) {
  Object.values(PAGES).forEach(id =>
    document.getElementById(id).classList.remove('active')
  );
  document.querySelectorAll('.nav-link[data-page]').forEach(link =>
    link.classList.toggle('active', link.dataset.page === pageKey)
  );
  const targetId = PAGES[pageKey];
  if (targetId) {
    document.getElementById(targetId).classList.add('active');
    onPageLoad(pageKey);
  }
}

function onPageLoad(pageKey) {
  if (pageKey === 'shelf')           loadShelf();
  if (pageKey === 'recommendations') { /* Step 4 */ }
}

// ── Login flow ────────────────────────────────────────────────

async function attemptLogin(username, password) {
  setLoginLoading(true);
  hideLoginError();
  saveSession(username, password);

  try {
    const response = await fetch(`${API_BASE}/api/books/search?query=test`, {
      headers: { 'Authorization': getAuthHeader() }
    });
    if (response.status === 401) throw Object.assign(new Error(), { status: 401 });
    if (!response.ok)            throw new Error(`HTTP ${response.status}`);
    showAppShell();
  } catch (err) {
    clearSession();
    if (err.status === 401) {
      showLoginError('Incorrect username or password.');
    } else if (err.message.includes('Failed to fetch')) {
      showLoginError('Cannot reach the server. Is the backend running on port 8080?');
    } else {
      showLoginError(`Unexpected error: ${err.message}`);
    }
  } finally {
    setLoginLoading(false);
  }
}

// ══════════════════════════════════════════════════════════════
// SEARCH
// ══════════════════════════════════════════════════════════════

async function searchBooks() {
  const query = document.getElementById('searchInput').value.trim();
  if (!query) return;

  const container = document.getElementById('searchResults');
  container.innerHTML = `
    <div class="col-12 text-center py-5">
      <div class="spinner-border" role="status"></div>
    </div>`;

  try {
    const books = await apiFetch(`/api/books/search?query=${encodeURIComponent(query)}`);
    renderSearchResults(books);
  } catch (err) {
    container.innerHTML = `
      <div class="col-12">
        <div class="alert alert-danger">Search failed: ${err.message}</div>
      </div>`;
  }
}

function renderSearchResults(books) {
  const container = document.getElementById('searchResults');

  // Handle both a raw array and a wrapped object e.g. { books: [...] }
  const list = Array.isArray(books) ? books : (books.books || books.results || []);

  if (!list.length) {
    container.innerHTML = `<div class="col-12 text-muted">No books found.</div>`;
    return;
  }

  container.innerHTML = list.map(book => `
    <div class="col">
      <div class="card h-100 shadow-sm book-card">
        <img
          src="${book.thumbnailUrl || 'https://placehold.co/128x192?text=No+Cover'}"
          class="card-img-top book-thumb"
          alt="${escapeHtml(book.title)}"
          onerror="this.src='https://placehold.co/128x192?text=No+Cover'"
        />
        <div class="card-body d-flex flex-column">
          <h6 class="card-title mb-1">${escapeHtml(book.title)}</h6>
          <p class="card-text text-muted small mb-3">${escapeHtml(book.author || book.authors || '')}</p>
          <button
            class="btn btn-add mt-auto"
            onclick="openAddModal('${escapeHtml(book.googleId)}', \`${escapeHtml(book.title)}\`)"
          >
            <i class="bi bi-plus-circle me-1"></i>Add to Shelf
          </button>
        </div>
      </div>
    </div>
  `).join('');
}

// ══════════════════════════════════════════════════════════════
// ADD TO SHELF  —  POST /api/v1/shelves/{googleBookId}?status=&rating=
// ══════════════════════════════════════════════════════════════

let _pendingGoogleId = null;

function openAddModal(googleId, title) {
  _pendingGoogleId = googleId;
  document.getElementById('modalBookTitle').textContent = title;
  document.getElementById('addStatus').value  = 'FINISHED';
  document.getElementById('addRating').value  = '0';
  document.getElementById('addModalError').style.display   = 'none';
  document.getElementById('addModalSuccess').style.display = 'none';
  document.getElementById('btnConfirmAdd').disabled = false;
  document.getElementById('btnConfirmAdd').innerHTML =
    '<i class="bi bi-check-lg me-1"></i>Add to Shelf';

  bootstrap.Modal.getOrCreateInstance(
    document.getElementById('addToShelfModal')
  ).show();
}

async function confirmAddToShelf() {
  const googleId = _pendingGoogleId;
  const status   = document.getElementById('addStatus').value;
  const rating   = document.getElementById('addRating').value;

  const btn = document.getElementById('btnConfirmAdd');
  btn.disabled = true;
  btn.innerHTML = '<span class="spinner-border spinner-border-sm me-1"></span>Adding…';

  document.getElementById('addModalError').style.display   = 'none';
  document.getElementById('addModalSuccess').style.display = 'none';

  try {
    await apiFetch(
      `/api/v1/shelves/${encodeURIComponent(googleId)}?status=${status}&rating=${rating}`,
      { method: 'POST' }
    );

    document.getElementById('addModalSuccess').style.display = 'block';
    btn.innerHTML = '<i class="bi bi-check-circle-fill me-1"></i>Added!';

    setTimeout(() => {
      bootstrap.Modal.getInstance(
        document.getElementById('addToShelfModal')
      ).hide();
    }, 1200);

  } catch (err) {
    const errEl = document.getElementById('addModalError');
    errEl.textContent = err.status === 409
      ? 'This book is already on your shelf.'
      : `Failed to add: ${err.message}`;
    errEl.style.display = 'block';
    btn.disabled = false;
    btn.innerHTML = '<i class="bi bi-check-lg me-1"></i>Add to Shelf';
  }
}

// ══════════════════════════════════════════════════════════════
// MY SHELF  —  GET /api/v1/shelves
// ══════════════════════════════════════════════════════════════

async function loadShelf() {
  const container = document.getElementById('shelfContent');
  container.innerHTML = `
    <div class="text-center py-5">
      <div class="spinner-border" role="status"></div>
    </div>`;

  try {
    const books = await apiFetch('/api/v1/shelves?limit=100');
    renderShelf(books);
  } catch (err) {
    container.innerHTML = `
      <div class="alert alert-danger">Could not load shelf: ${err.message}</div>`;
  }
}

function renderShelf(books) {
  const container = document.getElementById('shelfContent');

  if (!books || !books.length) {
    container.innerHTML = `
      <div class="text-center text-muted py-5">
        <i class="bi bi-bookshelf fs-1 d-block mb-3"></i>
        Your shelf is empty. Search for a book and add one!
      </div>`;
    return;
  }

  // Group by bookStatus
  const groups = {};
  books.forEach(b => {
    const s = b.bookStatus || 'UNKNOWN';
    if (!groups[s]) groups[s] = [];
    groups[s].push(b);
  });

  const statusLabel = {
    FINISHED:     '✅ Finished',
    CURRENTLY_READING:      '📖 Currently Reading',
    WANT_TO_READ: '🔖 Want to Read',
    UNKNOWN:      'Other',
  };

  // Preferred order
  const order = ['CURRENTLY_READING', 'WANT_TO_READ', 'FINISHED', 'UNKNOWN'];
  const sorted = order.filter(s => groups[s]).concat(
    Object.keys(groups).filter(s => !order.includes(s))
  );

  container.innerHTML = sorted.map(status => `
    <div class="mb-5">
      <h5 class="shelf-group-title">${statusLabel[status] || status}</h5>
      <div class="row row-cols-1 row-cols-md-2 row-cols-lg-3 g-3">
        ${groups[status].map(b => shelfCard(b)).join('')}
      </div>
    </div>
  `).join('');
}

function shelfCard(b) {
  const stars  = renderStars(b.userRating || 0);
  const pct    = b.finishedPercentage > 0
    ? `<div class="progress mt-2" style="height:4px;">
         <div class="progress-bar" style="width:${b.finishedPercentage}%;background:var(--amber)"></div>
       </div>
       <div class="text-muted small mt-1">${b.finishedPercentage}% read</div>`
    : '';
  const review = b.review
    ? `<p class="shelf-review fst-italic text-muted small mt-2">"${escapeHtml(b.review)}"</p>`
    : '';

  // Store bookTitle on the button so delete/edit can resolve it
  const safeTitle = escapeHtml(b.bookTitle || '');

  return `
    <div class="col">
      <div class="card h-100 shadow-sm shelf-card">
        <div class="card-body">
          <h6 class="card-title mb-1">${safeTitle}</h6>
          <div class="mb-1">${stars}</div>
          ${pct}
          ${review}
        </div>
        <div class="card-footer d-flex gap-2 bg-transparent border-0 pb-3">
          <button class="btn btn-sm btn-outline-secondary"
            onclick="openUpdateModal('${safeTitle}', ${b.userRating||0}, '${b.bookStatus||'FINISHED'}', ${b.finishedPercentage||0})">
            <i class="bi bi-pencil"></i> Edit
          </button>
          <button class="btn btn-sm btn-outline-danger"
            onclick="deleteFromShelf('${safeTitle}', this)">
            <i class="bi bi-trash"></i>
          </button>
        </div>
      </div>
    </div>`;
}

function renderStars(rating) {
  if (!rating) return '<span class="text-muted small">No rating</span>';
  let html = '';
  for (let i = 1; i <= 5; i++) {
    html += `<i class="bi bi-star${i <= rating ? '-fill' : ''} text-warning small"></i>`;
  }
  return html;
}

// ── Delete from shelf ─────────────────────────────────────────
// ⚠️ Note: the shelf response doesn't include googleId (only bookTitle/bookId).
// We resolve the googleId by searching for the title first.
// The cleanest long-term fix is to add googleId to UserBookListItemResponse.

async function deleteFromShelf(bookTitle, btnEl) {
  if (!confirm(`Remove "${bookTitle}" from your shelf?`)) return;

  btnEl.disabled = true;
  btnEl.innerHTML = '<span class="spinner-border spinner-border-sm"></span>';

  try {
    const googleId = await resolveGoogleId(bookTitle);
    await apiFetch(`/api/v1/shelves/${encodeURIComponent(googleId)}`, { method: 'DELETE' });
    loadShelf();
  } catch (err) {
    alert(`Delete failed: ${err.message}`);
    btnEl.disabled = false;
    btnEl.innerHTML = '<i class="bi bi-trash"></i>';
  }
}

// ── Update shelf entry ────────────────────────────────────────

let _updateBookTitle = null;

function openUpdateModal(bookTitle, currentRating, currentStatus, currentPct) {
  _updateBookTitle = bookTitle;

  document.getElementById('updateModalTitle').textContent = bookTitle;
  document.getElementById('updateStatus').value = currentStatus || 'FINISHED';
  document.getElementById('updateRating').value = currentRating || 0;
  document.getElementById('updatePct').value    = currentPct    || 0;
  document.getElementById('updateModalError').style.display = 'none';
  document.getElementById('btnConfirmUpdate').disabled = false;
  document.getElementById('btnConfirmUpdate').innerHTML =
    '<i class="bi bi-check-lg me-1"></i>Save Changes';

  bootstrap.Modal.getOrCreateInstance(
    document.getElementById('updateShelfModal')
  ).show();
}

async function confirmUpdate() {
  const status = document.getElementById('updateStatus').value;
  const rating = document.getElementById('updateRating').value;
  const pct    = document.getElementById('updatePct').value;
  const btn    = document.getElementById('btnConfirmUpdate');

  btn.disabled = true;
  btn.innerHTML = '<span class="spinner-border spinner-border-sm me-1"></span>Saving…';
  document.getElementById('updateModalError').style.display = 'none';

  try {
    const googleId = await resolveGoogleId(_updateBookTitle);
    const params   = new URLSearchParams({ status, rating });
    if (pct) params.set('finishedPercentage', pct);

    await apiFetch(
      `/api/v1/shelves/${encodeURIComponent(googleId)}/update?${params}`,
      { method: 'PATCH' }
    );

    bootstrap.Modal.getInstance(
      document.getElementById('updateShelfModal')
    ).hide();
    loadShelf();

  } catch (err) {
    document.getElementById('updateModalError').textContent = `Update failed: ${err.message}`;
    document.getElementById('updateModalError').style.display = 'block';
    btn.disabled = false;
    btn.innerHTML = '<i class="bi bi-check-lg me-1"></i>Save Changes';
  }
}

// ── Resolve googleId from a book title via search ─────────────

async function resolveGoogleId(title) {
  const results = await apiFetch(`/api/books/search?query=${encodeURIComponent(title)}`);
  const list    = Array.isArray(results) ? results : (results.books || results.results || []);
  const match   = list.find(b => b.title.toLowerCase() === title.toLowerCase());
  if (!match) throw new Error(`Could not resolve Google ID for "${title}". Try adding googleId to UserBookListItemResponse.`);
  return match.googleId;
}

// ══════════════════════════════════════════════════════════════
// UTILS
// ══════════════════════════════════════════════════════════════

function escapeHtml(str) {
  if (!str) return '';
  return String(str)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#39;');
}

// ══════════════════════════════════════════════════════════════
// EVENT LISTENERS
// ══════════════════════════════════════════════════════════════

document.addEventListener('DOMContentLoaded', () => {

  // ── Login ──
  document.getElementById('btnLogin').addEventListener('click', () => {
    const username = document.getElementById('inputUsername').value.trim();
    const password = document.getElementById('inputPassword').value;
    if (!username || !password) {
      showLoginError('Please enter both username and password.');
      return;
    }
    attemptLogin(username, password);
  });

  ['inputUsername', 'inputPassword'].forEach(id => {
    document.getElementById(id).addEventListener('keydown', e => {
      if (e.key === 'Enter') document.getElementById('btnLogin').click();
    });
  });

  // ── Logout ──
  document.getElementById('btnLogout').addEventListener('click', () => {
    clearSession();
    showLoginScreen();
    document.getElementById('inputUsername').value = '';
    document.getElementById('inputPassword').value = '';
  });

  // ── Navbar ──
  document.querySelectorAll('.nav-link[data-page]').forEach(link => {
    link.addEventListener('click', e => {
      e.preventDefault();
      navigateTo(link.dataset.page);
    });
  });

  // ── Search ──
  document.getElementById('btnSearch').addEventListener('click', searchBooks);
  document.getElementById('searchInput').addEventListener('keydown', e => {
    if (e.key === 'Enter') searchBooks();
  });

  // ── Add modal confirm ──
  document.getElementById('btnConfirmAdd').addEventListener('click', confirmAddToShelf);

  // ── Update modal confirm ──
  document.getElementById('btnConfirmUpdate').addEventListener('click', confirmUpdate);

  // ── Session restore ──
  if (getAuthHeader()) {
    showAppShell();
  } else {
    showLoginScreen();
  }
});
