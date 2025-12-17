package com.cervons.chickenroad;

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
        double balance = gameService.getUserBalance(id);
        return Response.ok(Map.of(
                "username", "User" + id,
                "balance", balance)).build();
    }
}
