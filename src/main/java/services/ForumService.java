package services;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import models.ForumTopic;
import orm.dao.ForumTopicDAO;
import orm.dao.ForumTopicDAOImpl;
import requests.CreateForumTopicRequest;

import javax.ws.rs.*;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;

@Api(value = "/forum", description = "Foro: temáticas")
@Path("/forum")
public class ForumService {

    private final ForumTopicDAO forumTopicDAO;

    public ForumService() {
        this.forumTopicDAO = new ForumTopicDAOImpl();
    }

    // ── GET /forum/topics ─────────────────────────────────────────────────────
    @GET
    @Path("/topics")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Listar todas las temáticas del foro")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Lista obtenida", response = ForumTopic.class, responseContainer = "List")
    })
    public Response getTopics() {
        List<ForumTopic> topics = forumTopicDAO.getTopics();
        GenericEntity<List<ForumTopic>> entity = new GenericEntity<List<ForumTopic>>(topics) {};
        return Response.status(200).entity(entity).build();
    }

    // ── GET /forum/topics/{id} ─────────────────────────────────────────────────
    @GET
    @Path("/topics/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Obtener una temática por ID")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Temática encontrada", response = ForumTopic.class),
            @ApiResponse(code = 404, message = "Temática no encontrada")
    })
    public Response getTopic(@PathParam("id") int id) {
        ForumTopic topic = forumTopicDAO.getTopic(id);
        if (topic == null) {
            return Response.status(404).entity("Temática no encontrada").build();
        }
        return Response.status(200).entity(topic).build();
    }

    // ── POST /forum/topics ────────────────────────────────────────────────────
    @POST
    @Path("/topics")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Crear una nueva temática en el foro")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Temática creada", response = ForumTopic.class),
            @ApiResponse(code = 400, message = "Datos inválidos")
    })
    public Response createTopic(CreateForumTopicRequest request) {
        if (request == null
                || request.getTitle() == null || request.getTitle().trim().isEmpty()
                || request.getAuthor() == null || request.getAuthor().trim().isEmpty()) {
            return Response.status(400).entity("Título y autor son obligatorios").build();
        }

        ForumTopic topic = new ForumTopic(
                0,
                request.getTitle().trim(),
                request.getDescription() != null ? request.getDescription().trim() : "",
                request.getAuthor().trim(),
                Instant.now().toString(),
                0
        );

        ForumTopic saved = forumTopicDAO.addTopic(topic);
        return Response.status(201).entity(saved).build();
    }
}
