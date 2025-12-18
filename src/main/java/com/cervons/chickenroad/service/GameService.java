package com.cervons.chickenroad.service;

import com.cervons.chickenroad.model.GameSession;
import com.cervons.chickenroad.model.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.redis.datasource.hash.HashCommands;
import io.quarkus.redis.datasource.value.ValueCommands;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

@ApplicationScoped
public class GameService {

    private final ValueCommands<String, String> commands;
    private final HashCommands<String, String, String> hashCommands;

    @Inject
    ObjectMapper mapper;

    private final Random random = new Random();

    public GameService(RedisDataSource ds) {
        this.commands = ds.value(String.class);
        this.hashCommands = ds.hash(String.class);
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

    public User getUser(String userId) {
        String key = "user:" + userId;
        Map<String, String> fields = hashCommands.hgetall(key);
        if (fields == null || fields.isEmpty()) {
            return new User(userId, "User" + userId, 1000.0, "EUR");
        }

        String username = fields.getOrDefault("username", fields.getOrDefault("usemame", "User" + userId));
        double balance = Double.parseDouble(fields.getOrDefault("Balance", "0.0"));
        String currency = fields.getOrDefault("currency", "EUR");

        return new User(userId, username, balance, currency);
    }

    public double getUserBalance(String userId) {
        String key = "user:" + userId;
        String bal = hashCommands.hget(key, "Balance");
        if (bal == null) {
            hashCommands.hset(key, "Balance", "1000.0");
            return 1000.0;
        }
        return Double.parseDouble(bal);
    }

    public void updateUserBalance(String userId, double delta) {
        double current = getUserBalance(userId);
        String key = "user:" + userId;
        hashCommands.hset(key, "Balance", String.valueOf(current + delta));
    }

    private List<Boolean> generatePath(String difficulty) {
        List<Boolean> path = new ArrayList<>();
        double safeProbability = getSafeProbability(difficulty);
        int maxSteps = getMaxSteps(difficulty);

        for (int i = 0; i < maxSteps; i++) {
            path.add(random.nextDouble() < safeProbability);
        }
        return path;
    }

    private int getMaxSteps(String difficulty) {
        if (difficulty == null)
            return 25;
        return switch (difficulty.toUpperCase()) {
            case "EASY" -> 25;
            case "MEDIUM" -> 20;
            case "HARD" -> 15;
            case "HARDCORE" -> 12;
            default -> 25;
        };
    }

    private double getSafeProbability(String difficulty) {
        if (difficulty == null)
            return 0.8;
        return switch (difficulty.toUpperCase()) {
            case "EASY" -> 24.0 / 25.0; // 1/25 BURN
            case "MEDIUM" -> 22.0 / 25.0; // 3/25 BURN
            case "HARD" -> 20.0 / 25.0; // 5/25 BURN
            case "HARDCORE" -> 15.0 / 25.0; // 10/25 BURN
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
