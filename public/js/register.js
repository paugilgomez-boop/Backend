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

  async function doRegister() {
    const email = ($email.val() || "").toString().trim();
    const username = ($username.val() || "").toString().trim();
    const password = ($password.val() || "").toString();
    const confirmPassword = ($password2.val() || "").toString();

    if (!email || !username || !password) {
      window.TD.showNotification("Rellena email, usuario y contraseña.", "warning");
      return;
    }
    if (password !== confirmPassword) {
      window.TD.showNotification("Las contraseñas no coinciden.", "warning");
      return;
    }

    setBusy(true);
    try {
      await window.TD.apiRequest("/auth/register", {
        method: "POST",
        body: {
          email,
          username,
          password,
          // Sensible defaults for a new player
          role: "PLAYER",
          saldo: 50,
          level: 1,
        },
      });

      window.TD.showNotification("Usuario registrado. Ahora inicia sesión.", "success");
      setTimeout(() => {
        window.location.href = "login.html";
      }, 1500);
    } catch (err) {
      window.TD.showNotification(err && err.message ? err.message : "No se pudo registrar.");
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
