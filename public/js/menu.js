$(document).ready(function () {
  const user = window.TD.requireUserOrRedirect();
  if (!user) return;

  $("#go-shop-btn").on("click", function () {
    window.location.href = "shop.html";
  });

  $("#go-users-btn").on("click", function () {
    window.location.href = "users.html";
  });

  $("#go-game-btn").on("click", function () {
    window.TD.showNotification("La pantalla de juego aun no esta conectada.", "warning");
  });

  $("#logout-btn").on("click", function () {
    window.TD.clearCurrentUser();
    window.location.href = "login.html";
  });
});
