$(function () {
    var currentUser = null;
    var items = [];
    var toast = bootstrap.Toast.getOrCreateInstance(document.getElementById("appToast"));

    loadItems();

    $("#registerRole").on("change", function () {
        var isAdmin = $(this).val() === "ADMIN";
        $("#levelGroup").toggleClass("d-none", isAdmin);
        $("#permissionsGroup").toggleClass("d-none", !isAdmin);
    });

    $("#loginForm").on("submit", function (event) {
        event.preventDefault();
        GameApi.login({
            username: $("#loginUsername").val(),
            password: $("#loginPassword").val()
        }).done(function (user) {
            currentUser = user;
            $("#sessionStatus").text("Sesion: " + user.username + " | saldo " + formatMoney(user.saldo));
            $("#buyPlayerId").val(user.id);
            $("#inventoryPlayerId").val(user.id);
            showToast("Login correcto.");
        }).fail(handleError);
    });

    $("#registerForm").on("submit", function (event) {
        event.preventDefault();
        var role = $("#registerRole").val();
        var payload = {
            id: $("#registerId").val(),
            username: $("#registerUsername").val(),
            password: $("#registerPassword").val(),
            email: $("#registerEmail").val(),
            saldo: numberValue("#registerSaldo"),
            role: role,
            level: numberValue("#registerLevel"),
            permissions: $("#registerPermissions").val()
        };

        GameApi.register(payload).done(function (user) {
            showToast("Usuario creado: " + user.username);
            $("#registerForm")[0].reset();
            $("#registerRole").trigger("change");
        }).fail(handleError);
    });

    $("#refreshItemsBtn").on("click", loadItems);

    $("#itemForm").on("submit", function (event) {
        event.preventDefault();
        var editingItemId = $("#editingItemId").val();
        var payload = readItemForm();
        var action = editingItemId ? GameApi.updateItem(editingItemId, payload) : GameApi.addItem(payload);

        action.done(function () {
            resetItemForm();
            loadItems();
            showToast(editingItemId ? "Item actualizado." : "Item creado.");
        }).fail(handleError);
    });

    $("#cancelEditBtn").on("click", resetItemForm);

    $("#itemsTableBody").on("click", "[data-action='edit']", function () {
        var item = findItem($(this).data("id"));
        if (!item) { return; }
        $("#editingItemId").val(item.id);
        $("#itemId").val(item.id).prop("disabled", true);
        $("#itemName").val(item.name);
        $("#itemType").val(item.type);
        $("#itemPrice").val(item.price);
        $("#itemDescription").val(item.description);
        $("#saveItemBtn").text("Actualizar");
    });

    $("#itemsTableBody").on("click", "[data-action='delete']", function () {
        GameApi.deleteItem($(this).data("id")).done(function () {
            loadItems();
            showToast("Item eliminado.");
        }).fail(handleError);
    });

    $("#buyForm").on("submit", function (event) {
        event.preventDefault();
        var playerId = $("#buyPlayerId").val();
        GameApi.buyItem(playerId, {
            itemId: $("#buyItemId").val(),
            quantity: numberValue("#buyQuantity")
        }).done(function () {
            $("#inventoryPlayerId").val(playerId);
            loadInventory(playerId);
            showToast("Compra realizada.");
        }).fail(handleError);
    });

    $("#loadInventoryBtn").on("click", function () {
        loadInventory($("#inventoryPlayerId").val());
    });

    function loadItems() {
        GameApi.getItems().done(function (response) {
            items = response || [];
            renderItems();
            renderItemOptions();
        }).fail(function () {
            items = [];
            renderItems();
            renderItemOptions();
            showToast("No se pudieron cargar los items.");
        });
    }

    function loadInventory(playerId) {
        if (!playerId) {
            showToast("Introduce un player ID.");
            return;
        }

        GameApi.getInventory(playerId).done(function (inventory) {
            renderInventory(inventory || []);
        }).fail(handleError);
    }

    function renderItems() {
        var $body = $("#itemsTableBody").empty();
        if (items.length === 0) {
            $body.append('<tr><td colspan="4" class="text-center text-muted py-4">Sin items cargados</td></tr>');
            return;
        }

        $.each(items, function (_, item) {
            $body.append(
                '<tr><td><span class="item-title">' + escapeHtml(item.name) + '</span>' +
                '<span class="item-description">' + escapeHtml(item.description || "Sin descripcion") + '</span></td>' +
                '<td><span class="badge rounded-pill badge-type">' + escapeHtml(item.type) + '</span></td>' +
                '<td class="text-end">' + formatMoney(item.price) + '</td>' +
                '<td class="text-end"><div class="btn-group btn-group-sm">' +
                '<button class="btn btn-outline-secondary" data-action="edit" data-id="' + escapeHtml(item.id) + '" type="button">Editar</button>' +
                '<button class="btn btn-outline-danger" data-action="delete" data-id="' + escapeHtml(item.id) + '" type="button">Borrar</button>' +
                '</div></td></tr>'
            );
        });
    }

    function renderItemOptions() {
        var $select = $("#buyItemId").empty();
        if (items.length === 0) {
            $select.append('<option value="">Sin items</option>');
            return;
        }

        $.each(items, function (_, item) {
            $select.append('<option value="' + escapeHtml(item.id) + '">' + escapeHtml(item.name) + ' - ' + formatMoney(item.price) + '</option>');
        });
    }

    function renderInventory(inventory) {
        var $cards = $("#inventoryCards").empty();
        if (inventory.length === 0) {
            $cards.append('<div class="col-12"><div class="empty-state">Este player no tiene items.</div></div>');
            return;
        }

        $.each(inventory, function (_, entry) {
            var item = findItem(entry.itemId);
            var itemName = item ? item.name : entry.itemId;
            $cards.append(
                '<div class="col-12 col-md-6 col-xl-4"><article class="inventory-card">' +
                '<div class="d-flex justify-content-between align-items-start gap-2"><h3>' + escapeHtml(itemName) + '</h3>' +
                '<span class="badge rounded-pill text-bg-success">x' + entry.quantity + '</span></div>' +
                '<div class="inventory-meta mt-2">Tipo: ' + escapeHtml(entry.type) + '</div>' +
                '<div class="inventory-meta">Item ID: ' + escapeHtml(entry.itemId) + '</div>' +
                '<div class="inventory-meta">Player ID: ' + escapeHtml(entry.playerId) + '</div>' +
                '</article></div>'
            );
        });
    }

    function readItemForm() {
        return {
            id: $("#itemId").val(),
            name: $("#itemName").val(),
            description: $("#itemDescription").val(),
            type: $("#itemType").val(),
            price: numberValue("#itemPrice")
        };
    }

    function resetItemForm() {
        $("#itemForm")[0].reset();
        $("#editingItemId").val("");
        $("#itemId").prop("disabled", false);
        $("#saveItemBtn").text("Guardar");
    }

    function findItem(itemId) {
        for (var index = 0; index < items.length; index++) {
            if (items[index].id === itemId) { return items[index]; }
        }
        return null;
    }

    function numberValue(selector) {
        return Number($(selector).val()) || 0;
    }

    function formatMoney(value) {
        return Number(value || 0).toFixed(2) + " coins";
    }

    function showToast(message) {
        $("#toastMessage").text(message);
        toast.show();
    }

    function handleError(xhr) {
        var message = xhr && xhr.responseText ? xhr.responseText : "Operacion no completada.";
        showToast(message);
    }

    function escapeHtml(value) {
        return String(value == null ? "" : value)
            .replace(/&/g, "&amp;")
            .replace(/</g, "&lt;")
            .replace(/>/g, "&gt;")
            .replace(/"/g, "&quot;")
            .replace(/'/g, "&#039;");
    }
});
