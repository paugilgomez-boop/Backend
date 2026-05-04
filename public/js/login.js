$(document).ready(function () {
  const $form = $("form");
  const $btn = $("#login-btn");
  const $username = $('input[name="username"]');
  const $password = $('input[name="password"]');

  function setBusy(isBusy) {
    $btn.prop("disabled", isBusy);
    $btn.css("opacity", isBusy ? 0.7 : 1);
  }

  async function doLogin() {
    const username = ($username.val() || "").toString().trim();
    const password = ($password.val() || "").toString();

    if (!username || !password) {
      alert("Introduce usuario y contraseña.");
      return;
    }

    setBusy(true);
    try {
      const user = await window.TD.apiRequest("/auth/login", {
        method: "POST",
        body: { username, password },
      });

      window.TD.saveCurrentUser(user);
      window.location.href = "menu.html";
    } catch (err) {
      alert(err && err.message ? err.message : "No se pudo iniciar sesión.");
    } finally {
      setBusy(false);
    }
  }

  $form.on("submit", function (e) {
    e.preventDefault();
    doLogin();
  });

  // Defensive: in case someone clicks the button but form submit doesn't fire
  $btn.on("click", function (e) {
    e.preventDefault();
    doLogin();
  });

  $("#toggle-password").on("click", function () {
    const isPw = $password.attr("type") === "password";
    $password.attr("type", isPw ? "text" : "password");
    const $icon = $("#toggle-password .material-symbols-outlined");
    if ($icon.length) $icon.text(isPw ? "visibility" : "visibility_off");
  });
});
