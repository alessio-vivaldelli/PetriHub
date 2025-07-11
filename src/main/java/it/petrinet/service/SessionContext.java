package it.petrinet.service;

import it.petrinet.model.User;

public class SessionContext {
    private static SessionContext instance;
    private User authenticatedUser;
    private String pendingMessage;

    private SessionContext() { }
    public static synchronized SessionContext getInstance() {
        if (instance == null) instance = new SessionContext();
        return instance;
    }

    /**
     * Set the authenticated user and clear any pending message.
     */
    public void setUser(User u) {
        this.authenticatedUser = u;
        this.pendingMessage = null;
    }

    /**
     * Returns the current user or null if not authenticated.
     */
    public User getUser() {
        return authenticatedUser;
    }

    /**
     * Clears session (user and pending message).
     */
    public void clear() {
        this.authenticatedUser = null;
        this.pendingMessage = null;
    }

    /**
     * Store a message to show after redirecting to login.
     */
    public void setPendingMessage(String message) {
        this.pendingMessage = message;
    }

    /**
     * Consume and return the pending message, then clear it.
     */
    public String consumePendingMessage() {
        String msg = this.pendingMessage;
        this.pendingMessage = null;
        return msg;
    }
}