package com.nttdata.dataspace.edc.fc.store.sql;

import java.util.List;
import java.util.Objects;

/**
 * Represents a participant in the federated catalog system.
 */
public class Participant {
    private String id;
    private String name;
    private String endpoint;
    private List<String> supportedProtocols;
    private long createdAt;
    private long updatedAt;

    // Default constructor
    public Participant() {}

    private Participant(String id, String name, String endpoint, List<String> supportedProtocols, long createdAt, long updatedAt) {
        this.id = id;
        this.name = name;
        this.endpoint = endpoint;
        this.supportedProtocols = supportedProtocols;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String id;
        private String name;
        private String endpoint;
        private List<String> supportedProtocols;
        private long createdAt;
        private long updatedAt;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder endpoint(String endpoint) {
            this.endpoint = endpoint;
            return this;
        }

        public Builder supportedProtocols(List<String> supportedProtocols) {
            this.supportedProtocols = supportedProtocols;
            return this;
        }

        public Builder createdAt(long createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder updatedAt(long updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public Participant build() {
            return new Participant(id, name, endpoint, supportedProtocols, createdAt, updatedAt);
        }
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEndpoint() { return endpoint; }
    public void setEndpoint(String endpoint) { this.endpoint = endpoint; }

    public List<String> getSupportedProtocols() { return supportedProtocols; }
    public void setSupportedProtocols(List<String> supportedProtocols) { this.supportedProtocols = supportedProtocols; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Participant that = (Participant) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}