$(document).ready(function () {
  const user = window.TD.requireUserOrRedirect();
  if (!user) return;

  const $userDisplay = $("#user-display");
  const $coinDisplay = $("#coin-display");
  const $itemsContainer = $("#shop-items-container");
  const $inventoryList = $("#inventory-list");
  let itemsById = {};

  function renderHeader(u) {
    if ($userDisplay.length) $userDisplay.text(u.username || u.id || "Usuario");
    if ($coinDisplay.length) $coinDisplay.text(u.saldo != null ? u.saldo : "0");
  }

  function renderItems(items) {
    if (!$itemsContainer.length) return;
    itemsById = {};
    if (!Array.isArray(items) || items.length === 0) {
      $itemsContainer.html(
        `<div class="glass-panel p-6 rounded-sm border border-white/10 text-slate-300">No hay items disponibles.</div>`
      );
      return;
    }

    items.forEach((it) => {
      if (it && it.id != null) {
        itemsById[String(it.id)] = it;
      }
    });

    const html = items
      .filter((it) => it && it.available !== false)
      .map((it) => {
        const price = it.price != null ? it.price : 0;
        const name = it.name || it.id || "Item";
        const desc = it.description || "";
        const asset = it.assetName ? `img/${it.assetName}` : 'img/default.png';
        return `
          <div class="glass-card rounded-sm p-5 flex flex-col gap-3">
            <div class="flex items-center gap-4">
              <div class="shrink-0 w-16 h-16 rounded-lg overflow-hidden border border-white/10 bg-black/40">
                <img src="${asset}" alt="${name}" class="w-full h-full object-cover">
              </div>
              <div class="min-w-0 flex-1">
                <div class="flex items-start justify-between gap-2">
                  <div class="text-lg font-bold text-white truncate">${name}</div>
                  <div class="shrink-0 text-neon-cyan-solid font-bold">${price}</div>
                </div>
                <div class="text-sm text-slate-300 line-clamp-2">${desc}</div>
              </div>
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
          const item = itemsById[String(itemId)];
          const itemName = item && item.name ? item.name : itemId;
          const qty = row.quantity != null ? row.quantity : 0;
          return `
            <div class="flex items-center justify-between rounded-sm px-3 py-2 bg-black/30 border border-white/10 gap-2">
              <div class="flex-1 min-w-0">
                <div class="font-semibold text-white truncate">${itemName}</div>
                <div class="text-[10px] text-slate-400">Cantidad: ${qty}</div>
              </div>
              <button class="sell-btn shrink-0 text-[10px] uppercase font-bold px-2 py-1 bg-secondary/10 hover:bg-secondary border border-secondary/50 text-secondary hover:text-white transition-all rounded-sm"
                data-item-id="${itemId}">
                Vender
              </button>
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

  $(document).on("click", ".sell-btn", async function () {
    const itemId = $(this).data("item-id");
    if (!itemId) return;
    try {
      const sale = await window.TD.apiRequest(`/players/${encodeURIComponent(user.id)}/inventory/${itemId}`, {
        method: "DELETE"
      });

      if (sale && sale.userSaldo != null) {
        user.saldo = sale.userSaldo;
        window.TD.saveCurrentUser(user);
      }

      window.TD.showNotification("Objeto vendido correctamente.", "success");
      await refreshAll();
    } catch (err) {
      window.TD.showNotification(err && err.message ? err.message : "No se pudo vender el item.");
    }
  });

  $(document).on("click", ".buy-btn", async function () {
    const itemId = $(this).data("item-id");
    if (!itemId) return;
    try {
      const purchase = await window.TD.apiRequest(`/players/${encodeURIComponent(user.id)}/inventory`, {
        method: "POST",
        body: { itemId, quantity: 1 },
      });

      // Update local user state with new balance from server
      if (purchase && purchase.userSaldo != null) {
        user.saldo = purchase.userSaldo;
        window.TD.saveCurrentUser(user);
      }

      await refreshAll();
    } catch (err) {
      window.TD.showNotification(err && err.message ? err.message : "No se pudo comprar el item.");
    }
  });

  $("#play-btn").on("click", function (e) {
    e.preventDefault();
    console.log("Play button clicked!");
    window.TD.showNotification("La pantalla de juego aun no esta conectada.", "warning");
  });

  $("#logout-btn").on("click", function () {
    window.TD.clearCurrentUser();
    window.location.href = "login.html";
  });

  refreshAll();
});
