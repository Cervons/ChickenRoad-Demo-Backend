package com.cervons.chickenroad;

import com.cervons.chickenroad.model.*;
import com.cervons.chickenroad.service.GameService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.Map;

@Path("/game")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class GameResource {

    @Inject
    GameService gameService;

    @POST
    @Path("/start")
    public Response startGame(StartGameRequest request) {
        try {
            GameSession session = gameService.createSession(request.userId(), request.bet(), request.difficulty());
            double balance = gameService.getUserBalance(request.userId());
            return Response.ok(Map.of(
                    "sessionId", session.sessionId,
                    "status", session.status,
                    "balance", balance)).build();
        } catch (Exception e) {
            return Response.status(400).entity(Map.of("error", e.getMessage())).build();
        }
    }

    @POST
    @Path("/next")
    public Response nextStep(NextStepRequest request) {
        GameSession session = gameService.getSession(request.sessionId());
        if (session == null)
            return Response.status(404).build();
        if (!"ACTIVE".equals(session.status)) {
            return Response.status(400).entity(Map.of("error", "Game not active")).build();
        }

        int nextStepIndex = session.currentStep;
        if (nextStepIndex >= session.path.size()) {
            return Response.status(400).entity(Map.of("error", "End of path")).build();
        }

        boolean isSafe = session.path.get(nextStepIndex);

        String event;

        if (isSafe) {
            session.currentStep++;
            session.multiplier = gameService.calculateMultiplier(session.currentStep, session.difficulty);
            event = "SAFE";
            gameService.saveSession(session);
        } else {
            session.status = "LOST";
            session.multiplier = 0;
            event = "BURN";
            gameService.saveSession(session);
        }

        return Response.ok(Map.of(
                "status", session.status,
                "multiplier", session.multiplier,
                "payout", session.bet * session.multiplier,
                "currentStep", session.currentStep,
                "event", event)).build();
    }

    @POST
    @Path("/cashout")
    public Response cashout(CashoutRequest request) {
        GameSession session = gameService.getSession(request.sessionId());
        if (session == null)
            return Response.status(404).build();
        if (!"ACTIVE".equals(session.status)) {
            return Response.status(400).entity(Map.of("error", "Game not active, status is " + session.status)).build();
        }

        session.status = "CASHOUT";
        double amountWon = session.bet * session.multiplier;
        gameService.updateUserBalance(session.userId, amountWon);
        gameService.saveSession(session);

        double newBalance = gameService.getUserBalance(session.userId);

        return Response.ok(Map.of(
                "balance", newBalance,
                "amountWon", amountWon)).build();
    }
}
