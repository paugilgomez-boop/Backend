package services;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import models.Inventory;
import models.Item;
import models.Purchase;
import models.User;
import repositories.GameManager;
import repositories.GameManagerImpl;
import requests.BuyItemRequest;
import requests.ItemRequest;
import requests.LoginRequest;
import requests.RegisterRequest;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.NoSuchElementException;

@Api(value = "/game", description = "Endpoint to Game Management Service")
@Path("/game")
public class GameService {

    private final GameManager gm;

    public GameService() {
        this.gm = GameManagerImpl.getInstance();
    }

    @POST
    @Path("/auth/register")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Registrar un player o admin")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Usuario registrado"),
            @ApiResponse(code = 400, message = "Datos invalidos")
    })
    public Response register(RegisterRequest request) {
        if (request == null) {
            return Response.status(400).entity("Datos de usuario invalidos").build();
        }
        try {
            User user = new User(request.getId(), request.getUsername(), request.getPassword(),
                    request.getEmail(), request.getSaldo(), resolvePermissions(request), request.getLevel());
            return Response.status(201).entity(gm.registerUser(user)).build();
        } catch (IllegalArgumentException e) {
            return Response.status(400).entity(e.getMessage()).build();
        }
    }

    @POST
    @Path("/auth/login")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Login de usuario")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Login correcto"),
            @ApiResponse(code = 401, message = "Credenciales invalidas")
    })
    public Response login(LoginRequest request) {
        if (request == null) {
            return Response.status(400).entity("Credenciales invalidas").build();
        }
        try {
            User user = gm.login(request.getUsername(), request.getPassword());
            return Response.status(200).entity(user).build();
        } catch (NoSuchElementException e) {
            return Response.status(401).entity(e.getMessage()).build();
        }
    }

    @POST
    @Path("/items")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Anadir item al catalogo")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Item creado"),
            @ApiResponse(code = 400, message = "Datos invalidos")
    })
    public Response addItem(ItemRequest request) {
        try {
            Item item = toItem(request);
            return Response.status(201).entity(gm.addItem(item)).build();
        } catch (IllegalArgumentException e) {
            return Response.status(400).entity(e.getMessage()).build();
        }
    }

    @PUT
    @Path("/items/{itemId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Modificar item")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Item modificado"),
            @ApiResponse(code = 400, message = "Datos invalidos"),
            @ApiResponse(code = 404, message = "Item no encontrado")
    })
    public Response updateItem(@PathParam("itemId") String itemId, ItemRequest request) {
        try {
            Item item = toItem(request);
            return Response.status(200).entity(gm.updateItem(itemId, item)).build();
        } catch (NoSuchElementException e) {
            return Response.status(404).entity(e.getMessage()).build();
        } catch (IllegalArgumentException e) {
            return Response.status(400).entity(e.getMessage()).build();
        }
    }

    @DELETE
    @Path("/items/{itemId}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Eliminar item")
    @ApiResponses(value = {
            @ApiResponse(code = 204, message = "Item eliminado"),
            @ApiResponse(code = 404, message = "Item no encontrado")
    })
    public Response deleteItem(@PathParam("itemId") String itemId) {
        try {
            gm.deleteItem(itemId);
            return Response.status(204).build();
        } catch (NoSuchElementException e) {
            return Response.status(404).entity(e.getMessage()).build();
        }
    }

    @GET
    @Path("/items")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Ver todos los items")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Consulta correcta", response = Item.class, responseContainer = "List")
    })
    public Response getAllItems() {
        List<Item> items = gm.getAllItems();
        GenericEntity<List<Item>> entity = new GenericEntity<List<Item>>(items) {};
        return Response.status(200).entity(entity).build();
    }

    @POST
    @Path("/players/{playerId}/inventory")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Comprar item y anadirlo al inventario")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Item comprado"),
            @ApiResponse(code = 400, message = "Datos invalidos"),
            @ApiResponse(code = 404, message = "Player o item no encontrado"),
            @ApiResponse(code = 409, message = "Saldo insuficiente")
    })
    public Response buyItem(@PathParam("playerId") String playerId, BuyItemRequest request) {
        if (request == null) {
            return Response.status(400).entity("Datos de compra invalidos").build();
        }
        try {
            Purchase purchase = gm.buyItem(playerId, request.getItemId(), request.getQuantity());
            return Response.status(201).entity(purchase).build();
        } catch (NoSuchElementException e) {
            return Response.status(404).entity(e.getMessage()).build();
        } catch (IllegalStateException e) {
            return Response.status(409).entity(e.getMessage()).build();
        } catch (IllegalArgumentException e) {
            return Response.status(400).entity(e.getMessage()).build();
        }
    }

    @GET
    @Path("/players/{playerId}/inventory")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Ver items de un player")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Consulta correcta", response = Inventory.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Player no encontrado")
    })
    public Response getInventoryByUser(@PathParam("playerId") String playerId) {
        try {
            List<Inventory> inventory = gm.getInventoryByUser(playerId);
            GenericEntity<List<Inventory>> entity = new GenericEntity<List<Inventory>>(inventory) {};
            return Response.status(200).entity(entity).build();
        } catch (NoSuchElementException e) {
            return Response.status(404).entity(e.getMessage()).build();
        }
    }

    @GET
    @Path("/players/{playerId}/purchases")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Ver compras de un usuario")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Consulta correcta", response = Purchase.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Usuario no encontrado")
    })
    public Response getPurchasesByPlayer(@PathParam("playerId") String playerId) {
        try {
            List<Purchase> purchases = gm.getPurchasesByUser(playerId);
            GenericEntity<List<Purchase>> entity = new GenericEntity<List<Purchase>>(purchases) {};
            return Response.status(200).entity(entity).build();
        } catch (NoSuchElementException e) {
            return Response.status(404).entity(e.getMessage()).build();
        }
    }

    private Item toItem(ItemRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Datos de item invalidos");
        }
        return new Item(request.getId(), request.getName(), request.getDescription(),
                request.getType(), request.getPrice(), request.isAvailable(), request.getAssetName());
    }

    private String resolvePermissions(RegisterRequest request) {
        if (request.getPermissions() != null && !request.getPermissions().trim().isEmpty()) {
            return request.getPermissions();
        }
        if (request.getRole() != null && !request.getRole().trim().isEmpty()) {
            return request.getRole();
        }
        return "PLAYER";
    }
}
