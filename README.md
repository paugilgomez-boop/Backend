# TowerDefence

Sistema integral (Backend + Frontend) para la gestión de usuarios, catálogo de items, inventarios y procesos de compra para un juego tipo Tower Defence.



### Backend (`src/main/java`)
- **`Main.java`**: Punto de entrada de la aplicación. Configura el servidor Grizzly y los manejadores de archivos estáticos.
- **`models/`**: Clases de dominio (`User`, `Item`, `Inventory`, `Purchase`).
- **`repositories/`**: Lógica de persistencia en memoria (Singleton `GameManagerImpl`).
- **`services/`**: Exposición de los endpoints REST (`GameService`).
- **`requests/`**: DTOs para las peticiones entrantes.
- **`resources/`**: Configuración de Log4j.

### Frontend (`public/`)
- **`index.html`**: Punto de entrada que redirige al login.
- **`login.html` / `register.html`**: Gestión de acceso de usuarios.
- **`shop.html`**: Interfaz para visualizar el catálogo y realizar compras.
- **`users.html`**: Vista de gestión de usuarios.
- **`js/`**:
    - `common.js`: Utilidades compartidas y configuración de la API (Base URL: `/dsaApp/game`).
    - `login.js`, `register.js`, `shop.js`: Lógica específica de cada página.
- **`swagger/`**: Interfaz interactiva de la API.

---

##  API REST (Endpoints)

La API base est� disponible en `http://localhost:8080/dsaApp/game`.

### Autenticación
- `POST /auth/register`: Registro de nuevos usuarios (Player/Admin).
- `POST /auth/login`: Validación de credenciales.

### Catálogo de items
- `GET /items`: Obtener la lista completa de items disponibles.
- `POST /items`: Añadir un nuevo item (Admin).
- `PUT /items/{itemId}`: Modificar un item existente (Admin).
- `DELETE /items/{itemId}`: Eliminar un item (Admin).

### Inventario y Compras
- `GET /players/{playerId}/inventory`: Ver los items que posee un jugador.
- `POST /players/{playerId}/inventory`: Realizar la compra de un item (resta saldo y añade al inventario).
- `GET /players/{playerId}/purchases`: Consultar el historial de compras de un usuario.

