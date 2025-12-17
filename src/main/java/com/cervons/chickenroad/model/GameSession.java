package com.cervons.chickenroad.model;

import java.util.List;

public class GameSession {
    public String sessionId;
    public String userId;
    public double bet;
    public String difficulty;
    public int currentStep;
    public double multiplier;
    public String status; // ACTIVE, LOST, CASHOUT
    public List<Boolean> path; // true = safe, false = burn

    public GameSession() {
    }
}
