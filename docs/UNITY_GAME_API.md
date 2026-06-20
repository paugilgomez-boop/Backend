# API Unity — Monedas y mejoras de torretas

Contrato REST para el cliente Unity (Tower Defense). Base URL en producción:

`https://dsa3.upc.edu/dsaApp/api/game`

En local: `http://localhost:8080/dsaApp/api/game`

No requiere autenticación en el MVP: el jugador se identifica con el query param `userId` (username guardado en Unity como `dsa_user_id`, o `"guest"` si no hay sesión).

El servidor **solo devuelve niveles comprados** (enteros ≥ 0). Los multiplicadores los aplica el cliente:

| Mejora | Fórmula en Unity |
|--------|------------------|
| Daño | `daño base × (1 + damageLevel × 0.15)` |
| Rango | `rango base × (1 + rangeLevel × 0.10)` |
| Vel. ataque | `firerate base × (1 + attackSpeedLevel × 0.12)` |

---

## POST `/coins/earn`

Suma monedas ganadas al completar un nivel. El saldo se guarda en el campo `saldo` del usuario en BD (compartido con la tienda web).

**Body** (`application/json`)

```json
{
  "userId": "marc",
  "coinsEarned": 265
}
```

| Campo | Tipo | Descripción |
|-------|------|-------------|
| `userId` | string | Username del jugador (PlayerPrefs `dsa_user_id`, o `"guest"`) |
| `coinsEarned` | int ≥ 1 | Monedas ganadas en la partida (Unity no llama si es 0) |

**Respuesta 200**

```json
{
  "userId": "marc",
  "coinsEarned": 265,
  "totalCoins": 530
}
```

| Campo | Descripción |
|-------|-------------|
| `coinsEarned` | Monedas de esta petición (confirmación) |
| `totalCoins` | Saldo total del usuario tras sumar |

**Errores**

| Código | Cuándo |
|--------|--------|
| 400 | `userId` vacío, `coinsEarned` ≤ 0, JSON inválido |
| 500 | Error interno |

Si el usuario no existe, se **crea automáticamente** con saldo inicial 0 y permisos `PLAYER` (adecuado para `guest` y jugadores sin registro previo).

**Ejemplo curl**

```bash
curl -s -X POST "https://dsa3.upc.edu/dsaApp/api/game/coins/earn" \
  -H "Content-Type: application/json" \
  -d '{"userId":"marc","coinsEarned":265}'
```

**Flujo completo con mejoras**

1. Jugador gana partida → `POST /coins/earn` (suma monedas al `saldo`)
2. Jugador entra al juego / tienda → `GET /upgrades` (lee niveles de torreta)
3. Tienda web gasta `saldo` para comprar mejoras (inventario items 2, 3, 4)

---

## GET `/upgrades`

Obtiene los niveles de mejora globales del usuario.

**Query params**

| Param | Tipo | Obligatorio | Descripción |
|-------|------|-------------|-------------|
| `userId` | string | sí | Username del jugador |

**Respuesta 200** (`Content-Type: application/json`)

```json
{
  "userId": "marc",
  "damageLevel": 2,
  "rangeLevel": 1,
  "attackSpeedLevel": 3
}
```

Los nombres de campo son exactos (camelCase). Si el usuario no existe o no tiene compras, se devuelve **200** con todos los niveles a `0`.

**Errores**

| Código | Cuándo |
|--------|--------|
| 400 | Falta `userId` o está vacío |
| 500 | Error interno |

**Ejemplos curl**

```bash
# Usuario con mejoras
curl -s "https://dsa3.upc.edu/dsaApp/api/game/upgrades?userId=marc"

# Usuario inexistente (niveles 0)
curl -s "https://dsa3.upc.edu/dsaApp/api/game/upgrades?userId=guest"

# userId con caracteres especiales (URL-encoded)
curl -s "https://dsa3.upc.edu/dsaApp/api/game/upgrades?userId=user%40test"
```

---

## POST `/upgrades/purchase` (tienda web, opcional)

Compra una mejora (+1 nivel). No lo usa Unity todavía.

**Body** (`application/json`)

```json
{
  "userId": "marc",
  "upgradeType": "damage"
}
```

`upgradeType`: `"damage"` | `"range"` | `"attackSpeed"`

**Respuesta 200**

```json
{
  "success": true,
  "userId": "marc",
  "damageLevel": 3,
  "rangeLevel": 1,
  "attackSpeedLevel": 3,
  "remainingCurrency": 150
}
```

**Precio por nivel:** `100 × 2^nivelActual` (100, 200, 400, …). Se descuenta del `saldo` del usuario en BD.

**Errores**

| Código | Cuándo |
|--------|--------|
| 400 | Datos inválidos o `upgradeType` desconocido |
| 404 | Usuario no encontrado |
| 409 | Saldo insuficiente |
| 500 | Error interno |

**Ejemplo curl**

```bash
curl -s -X POST "https://dsa3.upc.edu/dsaApp/api/game/upgrades/purchase" \
  -H "Content-Type: application/json" \
  -d '{"userId":"marc","upgradeType":"damage"}'
```

---

## CORS

El backend envía cabeceras CORS (`Access-Control-Allow-Origin: *`) para permitir clientes WebGL u otros orígenes.

---

## Persistencia (inventario)

Los niveles de mejora se almacenan en la tabla **`Inventory`**: la cantidad de cada item del catálogo representa el nivel comprado.

| Campo API | Item catálogo | ID |
|-----------|---------------|-----|
| `damageLevel` | Calibración de Cañón | 2 |
| `attackSpeedLevel` | Munición de Cadencia | 3 |
| `rangeLevel` | Lente de Precisión | 4 |

La tienda web (`POST /game/players/{id}/inventory`) y el endpoint Unity comparten el mismo inventario. Comprar esos items en la web también actualiza los niveles que lee Unity.

El endpoint `POST /upgrades/purchase` usa precio escalonado (`100 × 2^nivel`) y registra la compra en `Purchase` + incrementa el inventario.

---

## Casos de prueba

### Monedas

1. Usuario existente con 265 monedas gana 265 → `totalCoins` = 530.
2. Usuario nuevo gana 50 → se crea con `totalCoins` = 50.
3. `coinsEarned` = 0 → 400 (Unity no llama en este caso).
4. `userId` vacío → 400.

### Mejoras

1. `userId` existente con mejoras → niveles reales.
2. `userId` existente sin items de mejora en inventario → todos `0`.
3. `userId` inexistente → todos `0` (200).
4. Sin `userId` → 400.
5. `userId` URL-encoded → funciona correctamente.
