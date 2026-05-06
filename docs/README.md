# TowerDefence Docs

## UML de dominio

El diagrama principal actual esta en:

```text
docs/domain-uml.puml
```

Tambien se conserva una version anterior del modelo en:

```text
docs/domain-uml-previous.puml
```

Se puede abrir con el plugin de PlantUML de IntelliJ.

Este UML solo contiene clases del modelo:

- `User`
- `Item`
- `Inventory`
- `Purchase`

No incluye `GameManager`, `GameService` ni `Request`, porque esas clases son de implementacion y API REST, no del dominio del juego.

## Diferencia entre Inventory y Purchase

`Inventory` representa el estado actual:

```text
Que items tiene ahora un usuario.
```

`Purchase` representa el historial:

```text
Que compras ha realizado un usuario.
```

Para Unity, lo normal seria consultar `Inventory`, porque el juego necesita saber que objetos ya tiene disponibles el usuario.

## Rombos en el UML

El diagrama usa composicion con rombo negro en:

```text
User *-- Inventory
User *-- Purchase
```

Esto significa que `Inventory` y `Purchase` dependen del usuario: son datos asociados a un usuario concreto.

No se usa rombo entre `Item` e `Inventory` o `Purchase`, porque `Item` pertenece al catalogo de la tienda. El inventario y las compras solo lo referencian mediante `itemId`.

## IDs numericos

Los identificadores principales (`User.id`, `Item.id`, `Inventory.userId`, `Inventory.itemId`, `Purchase.id`, `Purchase.userId`, `Purchase.itemId`) son `int`.

Esto encaja con una futura base de datos con claves autoincrementales. En la implementacion actual en memoria, si se crea un usuario o item con `id <= 0`, el backend asigna el siguiente ID disponible.
