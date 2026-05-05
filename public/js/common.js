// Shared helpers for the HTML frontend (jQuery-based)
(function () {
  // Overwrite global alert to use our custom notification
  const nativeAlert = window.alert;
  window.alert = function (message) {
    console.log("Native alert intercepted:", message);
    // Use setTimeout to ensure TD is fully initialized if called early
    setTimeout(() => {
      if (window.TD && typeof window.TD.showNotification === "function") {
        window.TD.showNotification(message);
      } else {
        nativeAlert(message);
      }
    }, 1);
  };

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

  function showNotification(message, type = "error") {
    const id = "td-notification";
    let $el = $(`#${id}`);

    if ($el.length === 0) {
      $el = $(`
        <div id="${id}" style="z-index: 9999;" class="fixed top-6 left-1/2 -translate-x-1/2 w-full max-w-sm px-4 pointer-events-none transition-all duration-500 opacity-0 -translate-y-10">
          <div class="bg-[#1a0f0a]/90 backdrop-blur-xl border-2 border-primary/30 p-4 rounded-xl flex items-start gap-3 shadow-[0_0_30px_rgba(0,0,0,0.5)] pointer-events-auto relative overflow-hidden">
            <div class="absolute inset-x-0 top-0 h-[2px] bg-primary/50 shadow-[0_0_10px_rgba(34,197,94,0.4)]"></div>
            <div class="notification-icon mt-0.5"></div>
            <div class="notification-message flex-1 text-sm font-medium text-white/90 leading-relaxed"></div>
            <button class="notification-close text-white/30 hover:text-white transition-colors mt-0.5">
              <span class="material-symbols-outlined text-[18px]">close</span>
            </button>
          </div>
        </div>
      `).appendTo("body");

      $el.find(".notification-close").on("click", () => {
        hideNotification();
      });
    }

    const $panel = $el.find("> div");
    const $icon = $el.find(".notification-icon");
    const $msg = $el.find(".notification-message");
    const $topBar = $el.find(".absolute.inset-x-0.top-0");

    // Reset styles
    $panel.removeClass("border-primary/30 border-secondary/30 border-amber-500/30");
    $topBar.removeClass("bg-primary/50 bg-secondary/50 bg-amber-500/50");
    $topBar.removeClass("shadow-[0_0_10px_rgba(34,197,94,0.4)] shadow-[0_0_10px_rgba(239,68,68,0.4)] shadow-[0_0_10px_rgba(245,158,11,0.4)]");

    if (type === "success") {
      $panel.addClass("border-primary/30");
      $topBar.addClass("bg-primary/50 shadow-[0_0_10px_rgba(34,197,94,0.4)]");
      $icon.html('<span class="material-symbols-outlined text-primary text-[22px]">check_circle</span>');
    } else if (type === "warning") {
      $panel.addClass("border-amber-500/30");
      $topBar.addClass("bg-amber-500/50 shadow-[0_0_10px_rgba(245,158,11,0.4)]");
      $icon.html('<span class="material-symbols-outlined text-amber-500 text-[22px]">warning</span>');
    } else {
      $panel.addClass("border-secondary/30");
      $topBar.addClass("bg-secondary/50 shadow-[0_0_10px_rgba(239,68,68,0.4)]");
      $icon.html('<span class="material-symbols-outlined text-secondary text-[22px]">error</span>');
    }

    $msg.text(message);

    // Show
    $el.removeClass("opacity-0 -translate-y-10").addClass("opacity-100 translate-y-0");

    // Auto hide
    clearTimeout($el.data("timeout"));
    const timeout = setTimeout(() => {
      hideNotification();
    }, 5000);
    $el.data("timeout", timeout);
  }

  function hideNotification() {
    const $el = $("#td-notification");
    if ($el.length) {
      $el.addClass("opacity-0 -translate-y-10").removeClass("opacity-100 translate-y-0");
    }
  }

  // Expose a tiny global surface
  window.TD = {
    API_BASE_URL,
    apiRequest,
    saveCurrentUser,
    getCurrentUser,
    clearCurrentUser,
    requireUserOrRedirect,
    showNotification,
  };
})();
