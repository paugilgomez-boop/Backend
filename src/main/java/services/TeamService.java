package services;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import models.Team;
import responses.TeamInfoResponse;
import repositories.GameManager;
import repositories.GameManagerImpl;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.NoSuchElementException;

@Api(value = "/teams", description = "Endpoint to Team Management Service")
@Path("/teams")
public class TeamService {

    private final GameManager gm;

    public TeamService() {
        this.gm = GameManagerImpl.getInstance();
    }

    @GET
    @Path("/ranking")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Obtener el ranking de equipos")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Consulta correcta", response = Team.class, responseContainer = "List")
    })
    public Response getTeamsRanking() {
        List<Team> teams = gm.getTeamsRanking();
        GenericEntity<List<Team>> entity = new GenericEntity<List<Team>>(teams) {};
        return Response.status(200).entity(entity).build();
    }

    @PUT
    @Path("/join/{teamName}/{userName}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Unirse a un equipo")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Unido con exito", response = Team.class),
            @ApiResponse(code = 400, message = "Parametros invalidos"),
            @ApiResponse(code = 404, message = "Usuario no encontrado")
    })
    public Response joinTeam(@PathParam("teamName") String teamName, @PathParam("userName") String userName) {
        try {
            Team team = gm.joinTeam(teamName, userName);
            return Response.status(200).entity(team).build();
        } catch (IllegalArgumentException e) {
            return Response.status(400).entity(e.getMessage()).build();
        } catch (NoSuchElementException e) {
            return Response.status(404).entity(e.getMessage()).build();
        }
    }

    @GET
    @Path("/user/{userName}/team")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Obtener informacion del equipo de un usuario")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Consulta correcta", response = TeamInfoResponse.class),
            @ApiResponse(code = 404, message = "El usuario no pertenece a ningun equipo o no existe")
    })
    public Response getMyTeamInfo(@PathParam("userName") String userName) {
        try {
            TeamInfoResponse info = gm.getMyTeamInfo(userName);
            return Response.status(200).entity(info).build();
        } catch (IllegalArgumentException e) {
            return Response.status(400).entity(e.getMessage()).build();
        } catch (NoSuchElementException e) {
            return Response.status(404).entity(e.getMessage()).build();
        }
    }

    @DELETE
    @Path("/leave/{userName}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Abandonar el equipo actual")
    @ApiResponses(value = {
            @ApiResponse(code = 204, message = "Abandonado con exito"),
            @ApiResponse(code = 400, message = "Parametros invalidos"),
            @ApiResponse(code = 404, message = "Usuario no encontrado o no tiene equipo")
    })
    public Response leaveTeam(@PathParam("userName") String userName) {
        try {
            gm.leaveTeam(userName);
            return Response.status(204).build();
        } catch (IllegalArgumentException e) {
            return Response.status(400).entity(e.getMessage()).build();
        } catch (NoSuchElementException e) {
            return Response.status(404).entity(e.getMessage()).build();
        }
    }
}
