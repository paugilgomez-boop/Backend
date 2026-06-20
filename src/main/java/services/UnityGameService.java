package services;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import repositories.GameManager;
import repositories.GameManagerImpl;
import requests.EarnCoinsRequest;
import requests.PurchaseUpgradeRequest;
import responses.EarnCoinsResponse;
import responses.GameUpgradePurchaseResponse;
import responses.GameUpgradesResponse;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.NoSuchElementException;

@Api(value = "/api/game", description = "API de mejoras para cliente Unity")
@Path("/api/game")
public class UnityGameService {

    private final GameManager gm;

    public UnityGameService() {
        this.gm = GameManagerImpl.getInstance();
    }

    @GET
    @Path("/upgrades")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Obtener niveles de mejora de torretas de un usuario")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Niveles de mejora", response = GameUpgradesResponse.class),
            @ApiResponse(code = 400, message = "userId invalido")
    })
    public Response getUpgrades(@QueryParam("userId") String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            return Response.status(400).entity("userId requerido").build();
        }
        try {
            GameUpgradesResponse response = gm.getUpgradesByUsername(userId.trim());
            return Response.ok(response).build();
        } catch (Exception e) {
            return Response.status(500).entity("Error interno").build();
        }
    }

    @POST
    @Path("/coins/earn")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Sumar monedas ganadas al completar un nivel")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Monedas acreditadas", response = EarnCoinsResponse.class),
            @ApiResponse(code = 400, message = "Datos invalidos")
    })
    public Response earnCoins(EarnCoinsRequest request) {
        if (request == null || request.getUserId() == null || request.getUserId().trim().isEmpty()) {
            return Response.status(400).entity("userId requerido").build();
        }
        try {
            EarnCoinsResponse response = gm.earnCoins(request.getUserId().trim(), request.getCoinsEarned());
            return Response.ok(response).build();
        } catch (IllegalArgumentException e) {
            return Response.status(400).entity(e.getMessage()).build();
        } catch (Exception e) {
            return Response.status(500).entity("Error interno").build();
        }
    }

    @POST
    @Path("/upgrades/purchase")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Comprar una mejora de torreta")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Compra realizada", response = GameUpgradePurchaseResponse.class),
            @ApiResponse(code = 400, message = "Datos invalidos"),
            @ApiResponse(code = 404, message = "Usuario no encontrado"),
            @ApiResponse(code = 409, message = "Saldo insuficiente")
    })
    public Response purchaseUpgrade(PurchaseUpgradeRequest request) {
        if (request == null || request.getUserId() == null || request.getUserId().trim().isEmpty()
                || request.getUpgradeType() == null || request.getUpgradeType().trim().isEmpty()) {
            return Response.status(400).entity("userId y upgradeType requeridos").build();
        }
        try {
            GameUpgradePurchaseResponse response = gm.purchaseUpgrade(
                    request.getUserId().trim(),
                    request.getUpgradeType().trim()
            );
            return Response.ok(response).build();
        } catch (NoSuchElementException e) {
            return Response.status(404).entity(e.getMessage()).build();
        } catch (IllegalStateException e) {
            return Response.status(409).entity(e.getMessage()).build();
        } catch (IllegalArgumentException e) {
            return Response.status(400).entity(e.getMessage()).build();
        } catch (Exception e) {
            return Response.status(500).entity("Error interno").build();
        }
    }
}
