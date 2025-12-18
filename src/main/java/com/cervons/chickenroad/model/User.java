package com.cervons.chickenroad.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class User {
    private String userId;

    @JsonProperty("Balance")
    private double balance;

    public User() {
    }

    public User(String userId, double balance) {
        this.userId = userId;
        this.balance = balance;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }
}
