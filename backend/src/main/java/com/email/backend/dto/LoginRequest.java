package com.email.backend.dto;

public class LoginRequest {
    private String email;
    private String password;

    public LoginRequest() {}

    public LoginRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }


    public static class GraphNode {
        private String id;
        private String label;
        private String path;
        private String language;
        private String type;
        private int weight;
    }
}
