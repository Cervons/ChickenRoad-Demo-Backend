package com.cervons.chickenroad.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class User {
    private String userId;

    @JsonProperty("username")
    private String username;

    @JsonProperty("Balance")
    private double balance;

    @JsonProperty("currency")
    private String currency;

    public User() {
    }

    public User(String userId, String username, double balance, String currency) {
        this.userId = userId;
        this.username = username;
        this.balance = balance;
        this.currency = currency;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }
}
