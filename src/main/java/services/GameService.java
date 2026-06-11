package services;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import models.Inventory;
import models.Item;
import models.Purchase;
import models.User;
import models.GameEvent;
import models.TeamMember;
import models.TeamResponse;
import repositories.GameManager;
import repositories.GameManagerImpl;
import requests.BuyItemRequest;
import requests.EventRegistrationRequest;
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
    public Response updateItem(@PathParam("itemId") int itemId, ItemRequest request) {
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
    public Response deleteItem(@PathParam("itemId") int itemId) {
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

    @GET
    @Path("/users")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Ver todos los usuarios")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Consulta correcta", response = User.class, responseContainer = "List")
    })
    public Response getAllUsers() {
        List<User> users = gm.getAllUsers();
        GenericEntity<List<User>> entity = new GenericEntity<List<User>>(users) {};
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
    public Response buyItem(@PathParam("playerId") int playerId, BuyItemRequest request) {
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

    @DELETE
    @Path("/players/{playerId}/inventory/{itemId}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Vender un item y devolver monedas")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Item vendido"),
            @ApiResponse(code = 404, message = "Player o item no encontrado"),
            @ApiResponse(code = 400, message = "No tienes el item o cantidad invalida")
    })
    public Response sellItem(@PathParam("playerId") int playerId, @PathParam("itemId") int itemId) {
        try {
            Purchase purchase = gm.sellItem(playerId, itemId, 1);
            return Response.status(200).entity(purchase).build();
        } catch (NoSuchElementException e) {
            return Response.status(404).entity(e.getMessage()).build();
        } catch (IllegalStateException | IllegalArgumentException e) {
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
    public Response getInventoryByUser(@PathParam("playerId") int playerId) {
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
    public Response getPurchasesByPlayer(@PathParam("playerId") int playerId) {
        try {
            List<Purchase> purchases = gm.getPurchasesByUser(playerId);
            GenericEntity<List<Purchase>> entity = new GenericEntity<List<Purchase>>(purchases) {};
            return Response.status(200).entity(entity).build();
        } catch (NoSuchElementException e) {
            return Response.status(404).entity(e.getMessage()).build();
        }
    }

    @GET
    @Path("/events")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Listado de eventos")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Consulta correcta", response = GameEvent.class, responseContainer = "List")
    })
    public Response getEvents() {
        System.out.println("[MINIM2-EJ4] GET /game/events called");

        List<GameEvent> events = gm.getEvents();
        GenericEntity<List<GameEvent>> entity = new GenericEntity<List<GameEvent>>(events) {};
        return Response.status(200).entity(entity).build();
    }

    @POST
    @Path("/events/{eventId}/register")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Registrarse en un evento")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Usuario registrado en el evento"),
            @ApiResponse(code = 400, message = "Datos invalidos"),
            @ApiResponse(code = 404, message = "Usuario o evento no encontrado"),
            @ApiResponse(code = 409, message = "Usuario ya inscrito")
    })
    public Response registerToEvent(@PathParam("eventId") int eventId, EventRegistrationRequest request) {
        System.out.println("[MINIM2-EJ4] POST /game/events/" + eventId + "/register called");

        if (request == null) {
            return Response.status(400).entity("Datos de registro invalidos").build();
        }

        try {
            System.out.println("[MINIM2-EJ4] userId=" + request.getUserId()
                    + ", username=" + request.getUsername()
                    + ", eventId=" + eventId);

            return Response.status(200)
                    .entity(gm.registerToEvent(eventId, request.getUserId(), request.getUsername()))
                    .build();

        } catch (NoSuchElementException e) {
            return Response.status(404).entity(e.getMessage()).build();
        } catch (IllegalStateException e) {
            return Response.status(409).entity(e.getMessage()).build();
        } catch (IllegalArgumentException e) {
            return Response.status(400).entity(e.getMessage()).build();
        }
    }

    @GET
    @Path("/user/{username}/team")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Obtener el equipo y miembros de un usuario")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Consulta correcta", response = TeamResponse.class)
    })
    public Response getUserTeam(@PathParam("username") String username) {
        System.out.println("[MINIM2] GET /game/user/" + username + "/team called");
        
        java.util.List<TeamMember> members = new java.util.ArrayList<>();
        members.add(new TeamMember("Juan", "https://cdn.pixabay.com/photo/2017/07/11/15/51/kermit-2499379_1280.png", 250));
        members.add(new TeamMember("Palomo", "https://cdn.pixabay.com/photo/2016/03/31/19/58/avatar-1295397_1280.png", 200));
        members.add(new TeamMember("Sergi", "https://cdn.pixabay.com/photo/2015/04/23/22/00/tree-736885_1280.jpg", 150));
        
        TeamResponse response = new TeamResponse("porxinos", members);
        return Response.status(200).entity(response).build();
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
