// Shared helpers for the HTML frontend (jQuery-based)
(function () {
  // Backend REST base is served under `/dsaApp` (see `Main.BASE_URI`)
  const API_BASE_URL = `${window.location.origin}/dsaApp/game`;

  const STORAGE_KEY = "td_current_user";

  function parseJsonSafely(text) {
    try {
      return JSON.parse(text);
    } catch (_) {
      return null;
    }
  }

  function normalizeError(jqXHR) {
    const raw = jqXHR && typeof jqXHR.responseText === "string" ? jqXHR.responseText : "";
    const json = raw ? parseJsonSafely(raw) : null;
    const msg =
      (json && (json.message || json.error)) ||
      raw ||
      (jqXHR && jqXHR.status ? `Error HTTP ${jqXHR.status}` : "Error de red");
    return new Error(msg);
  }

  async function apiRequest(path, { method = "GET", body } = {}) {
    return new Promise((resolve, reject) => {
      $.ajax({
        url: `${API_BASE_URL}${path}`,
        method,
        data: body != null ? JSON.stringify(body) : undefined,
        contentType: body != null ? "application/json" : undefined,
        dataType: "json",
        success: (data) => resolve(data),
        error: (jqXHR) => reject(normalizeError(jqXHR)),
      });
    });
  }

  function saveCurrentUser(user) {
    if (!user) return;
    localStorage.setItem(STORAGE_KEY, JSON.stringify(user));
  }

  function getCurrentUser() {
    const raw = localStorage.getItem(STORAGE_KEY);
    if (!raw) return null;
    const parsed = parseJsonSafely(raw);
    return parsed && typeof parsed === "object" ? parsed : null;
  }

  function clearCurrentUser() {
    localStorage.removeItem(STORAGE_KEY);
  }

  function requireUserOrRedirect() {
    const user = getCurrentUser();
    if (!user) {
      window.location.href = "login.html";
      return null;
    }
    return user;
  }

  // Expose a tiny global surface
  window.TD = {
    API_BASE_URL,
    apiRequest,
    saveCurrentUser,
    getCurrentUser,
    clearCurrentUser,
    requireUserOrRedirect,
  };
})();
