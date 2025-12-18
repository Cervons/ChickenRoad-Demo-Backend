package com.cervons.chickenroad;

import com.cervons.chickenroad.model.*;
import com.cervons.chickenroad.service.GameService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.Map;

@Path("/user")
@Produces(MediaType.APPLICATION_JSON)
public class UserResource {

    @Inject
    GameService gameService;

    @GET
    @Path("/{id}")
    public Response getUser(@PathParam("id") String id) {
        User user = gameService.getUser(id);
        return Response.ok(user).build();
    }

    @POST
    @Path("/deposit")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response deposit(DepositRequest request) {
        if (request.amount() <= 0) {
            return Response.status(400).entity(Map.of("error", "Amount must be positive")).build();
        }

        gameService.updateUserBalance(request.userId(), request.amount());
        User updatedUser = gameService.getUser(request.userId());

        return Response.ok(updatedUser).build();
    }
}
