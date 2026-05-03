$(document).ready(function () {
  const user = window.TD.requireUserOrRedirect();
  if (!user) return;

  const $userDisplay = $("#user-display");
  const $coinDisplay = $("#coin-display");
  const $itemsContainer = $("#shop-items-container");
  const $inventoryList = $("#inventory-list");

  function renderHeader(u) {
    if ($userDisplay.length) $userDisplay.text(u.username || u.id || "Usuario");
    if ($coinDisplay.length) $coinDisplay.text(u.saldo != null ? u.saldo : "0");
  }

  function renderItems(items) {
    if (!$itemsContainer.length) return;
    if (!Array.isArray(items) || items.length === 0) {
      $itemsContainer.html(
        `<div class="glass-panel p-6 rounded-sm border border-white/10 text-slate-300">No hay items disponibles.</div>`
      );
      return;
    }

    const html = items
      .filter((it) => it && it.available !== false)
      .map((it) => {
        const price = it.price != null ? it.price : 0;
        const name = it.name || it.id || "Item";
        const desc = it.description || "";
        return `
          <div class="glass-card rounded-sm p-5 flex flex-col gap-3">
            <div class="flex items-start justify-between gap-4">
              <div class="min-w-0">
                <div class="text-lg font-bold text-white truncate">${name}</div>
                <div class="text-sm text-slate-300">${desc}</div>
              </div>
              <div class="shrink-0 text-neon-cyan-solid font-bold">${price}</div>
            </div>
            <button class="buy-btn mt-2 rounded-sm h-10 px-4 bg-neon-cyan-solid/10 hover:bg-neon-cyan-solid border border-neon-cyan-solid text-neon-cyan-solid text-sm font-bold transition-all hover:text-black"
              data-item-id="${it.id}">
              Comprar
            </button>
          </div>
        `;
      })
      .join("");

    $itemsContainer.html(html);
  }

  function renderInventory(inv) {
    if (!$inventoryList.length) return;
    if (!Array.isArray(inv) || inv.length === 0) {
      $inventoryList.html(
        `<div class="text-slate-300 text-sm">Tu inventario está vacío.</div>`
      );
      return;
    }
    $inventoryList.html(
      inv
        .map((row) => {
          const itemId = row.itemId || row.itemID || row.item || "Item";
          const qty = row.quantity != null ? row.quantity : 0;
          return `
            <div class="flex items-center justify-between rounded-sm px-3 py-2 bg-black/30 border border-white/10">
              <div class="font-semibold text-white">${itemId}</div>
              <div class="text-slate-200">x${qty}</div>
            </div>
          `;
        })
        .join("")
    );
  }

  async function refreshAll() {
    renderHeader(user);
    try {
      const [items, inv] = await Promise.all([
        window.TD.apiRequest("/items"),
        window.TD.apiRequest(`/players/${encodeURIComponent(user.id)}/inventory`),
      ]);
      renderItems(items);
      renderInventory(inv);
    } catch (err) {
      // Keep page usable even if API fails
      console.error(err);
    }
  }

  $(document).on("click", ".buy-btn", async function () {
    const itemId = $(this).data("item-id");
    if (!itemId) return;
    try {
      await window.TD.apiRequest(`/players/${encodeURIComponent(user.id)}/inventory`, {
        method: "POST",
        body: { itemId, quantity: 1 },
      });
      // backend updates saldo on the server-side user instance; refresh user from local store is stale
      // simplest approach: re-login is overkill; just refresh inventory & items and keep saldo display as-is
      await refreshAll();
    } catch (err) {
      alert(err && err.message ? err.message : "No se pudo comprar el item.");
    }
  });

  $("#logout-btn").on("click", function () {
    window.TD.clearCurrentUser();
    window.location.href = "login.html";
  });

  refreshAll();
});
