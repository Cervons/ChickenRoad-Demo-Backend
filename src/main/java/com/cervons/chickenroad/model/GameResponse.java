package com.cervons.chickenroad.model;

public record GameResponse(
        String sessionId,
        String status,
        double multiplier,
        int currentStep,
        double payout,
        double balance,
        String event) {
}
