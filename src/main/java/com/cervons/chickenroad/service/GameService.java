package com.cervons.chickenroad.service;

import com.cervons.chickenroad.model.GameSession;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.redis.datasource.value.ValueCommands;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@ApplicationScoped
public class GameService {

    private final ValueCommands<String, String> commands;

    @Inject
    ObjectMapper mapper;

    private final Random random = new Random();

    public GameService(RedisDataSource ds) {
        this.commands = ds.value(String.class);
    }

    public GameSession createSession(String userId, double bet, String difficulty) {
        double currentBalance = getUserBalance(userId);
        if (currentBalance < bet) {
            throw new RuntimeException("Insufficient balance");
        }
        updateUserBalance(userId, -bet);

        GameSession session = new GameSession();
        session.sessionId = UUID.randomUUID().toString();
        session.userId = userId;
        session.bet = bet;
        session.difficulty = difficulty;
        session.currentStep = 0;
        session.multiplier = 1.0;
        session.status = "ACTIVE";
        session.path = generatePath(difficulty);

        saveSession(session);
        return session;
    }

    public GameSession getSession(String sessionId) {
        String json = commands.get("session:" + sessionId);
        if (json == null)
            return null;
        try {
            return mapper.readValue(json, GameSession.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void saveSession(GameSession session) {
        try {
            commands.set("session:" + session.sessionId, mapper.writeValueAsString(session));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    public double getUserBalance(String userId) {
        String bal = commands.get("user:" + userId + ":balance");
        if (bal == null) {
            commands.set("user:" + userId + ":balance", "1000.0");
            return 1000.0;
        }
        return Double.parseDouble(bal);
    }

    public void updateUserBalance(String userId, double delta) {
        double current = getUserBalance(userId);
        commands.set("user:" + userId + ":balance", String.valueOf(current + delta));
    }

    private List<Boolean> generatePath(String difficulty) {
        List<Boolean> path = new ArrayList<>();
        // Simple logic: true = safe
        double safeProbability = getSafeProbability(difficulty);

        for (int i = 0; i < 25; i++) {
            path.add(random.nextDouble() < safeProbability);
        }
        return path;
    }

    private double getSafeProbability(String difficulty) {
        if (difficulty == null)
            return 0.8;
        return switch (difficulty.toUpperCase()) {
            case "EASY" -> 0.90;
            case "MEDIUM" -> 0.75;
            case "HARD" -> 0.50;
            case "HARDCORE" -> 0.25;
            default -> 0.80;
        };
    }

    public double calculateMultiplier(int step, String difficulty) {
        // Exponential multiplier
        double base = switch (difficulty != null ? difficulty.toUpperCase() : "MEDIUM") {
            case "EASY" -> 1.1;
            case "MEDIUM" -> 1.25;
            case "HARD" -> 1.5;
            case "HARDCORE" -> 2.0;
            default -> 1.25;
        };
        // step is 1-indexed for multiplier calculation
        return Math.round(Math.pow(base, step) * 100.0) / 100.0;
    }
}
