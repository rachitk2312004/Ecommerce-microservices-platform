package com.enterprise.ecommerce.inventory.dto;

public class CheckAvailabilityResponse {

    private boolean available;

    public CheckAvailabilityResponse() {
    }

    public CheckAvailabilityResponse(boolean available) {
        this.available = available;
    }

    public static Builder builder() {
        return new Builder();
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public static final class Builder {
        private boolean available;

        private Builder() {
        }

        public Builder available(boolean available) {
            this.available = available;
            return this;
        }

        public CheckAvailabilityResponse build() {
            return new CheckAvailabilityResponse(available);
        }
    }
}
