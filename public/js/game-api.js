var GameApi = (function () {
    var baseUrl = "/dsaApp/game";

    function request(path, method, data) {
        var options = {
            url: baseUrl + path,
            method: method,
            contentType: "application/json"
        };

        if (data) {
            options.data = JSON.stringify(data);
        }

        return $.ajax(options);
    }

    return {
        register: function (payload) { return request("/auth/register", "POST", payload); },
        login: function (payload) { return request("/auth/login", "POST", payload); },
        getItems: function () { return request("/items", "GET"); },
        addItem: function (payload) { return request("/items", "POST", payload); },
        updateItem: function (itemId, payload) { return request("/items/" + encodeURIComponent(itemId), "PUT", payload); },
        deleteItem: function (itemId) { return request("/items/" + encodeURIComponent(itemId), "DELETE"); },
        buyItem: function (playerId, payload) { return request("/players/" + encodeURIComponent(playerId) + "/inventory", "POST", payload); },
        getInventory: function (playerId) { return request("/players/" + encodeURIComponent(playerId) + "/inventory", "GET"); }
    };
})();
