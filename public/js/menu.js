$(document).ready(function () {
  const user = window.TD.requireUserOrRedirect();
  if (!user) return;

  $("#go-shop-btn").on("click", function () {
    window.location.href = "shop.html";
  });

  $("#go-game-btn").on("click", function () {
    alert("La pantalla de juego aun no esta conectada.");
  });

  $("#logout-btn").on("click", function () {
    window.TD.clearCurrentUser();
    window.location.href = "login.html";
  });
});
