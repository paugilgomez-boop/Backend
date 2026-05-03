$(document).ready(function () {
  const $form = $("form");
  const $btn = $("#register-btn");

  const $email = $('input[name="email"]');
  const $username = $('input[name="username"]');
  const $password = $('input[name="password"]');
  const $password2 = $('input[name="confirm_password"]');

  function setBusy(isBusy) {
    $btn.prop("disabled", isBusy);
    $btn.css("opacity", isBusy ? 0.7 : 1);
  }

  function makeUserId() {
    // Backend requires a non-empty id; keep it simple and unique enough for local use.
    return `U${Date.now()}`;
  }

  async function doRegister() {
    const email = ($email.val() || "").toString().trim();
    const username = ($username.val() || "").toString().trim();
    const password = ($password.val() || "").toString();
    const confirmPassword = ($password2.val() || "").toString();

    if (!email || !username || !password) {
      alert("Rellena email, usuario y contraseña.");
      return;
    }
    if (password !== confirmPassword) {
      alert("Las contraseñas no coinciden.");
      return;
    }

    setBusy(true);
    try {
      await window.TD.apiRequest("/auth/register", {
        method: "POST",
        body: {
          id: makeUserId(),
          email,
          username,
          password,
          // Sensible defaults for a new player
          role: "PLAYER",
          saldo: 50,
          level: 1,
        },
      });

      alert("Usuario registrado. Ahora inicia sesión.");
      window.location.href = "login.html";
    } catch (err) {
      alert(err && err.message ? err.message : "No se pudo registrar.");
    } finally {
      setBusy(false);
    }
  }

  $form.on("submit", function (e) {
    e.preventDefault();
    doRegister();
  });

  $btn.on("click", function (e) {
    e.preventDefault();
    doRegister();
  });

  $("#toggle-password-1").on("click", function () {
    const isPw = $password.attr("type") === "password";
    $password.attr("type", isPw ? "text" : "password");
    const $icon = $("#toggle-password-1 .material-symbols-outlined");
    if ($icon.length) $icon.text(isPw ? "visibility" : "visibility_off");
  });

  $("#toggle-password-2").on("click", function () {
    const isPw = $password2.attr("type") === "password";
    $password2.attr("type", isPw ? "text" : "password");
    const $icon = $("#toggle-password-2 .material-symbols-outlined");
    if ($icon.length) $icon.text(isPw ? "visibility" : "visibility_off");
  });
});
