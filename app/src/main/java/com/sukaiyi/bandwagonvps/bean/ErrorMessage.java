package com.sukaiyi.bandwagonvps.bean;

public class ErrorMessage {
    private int error;
    private String message;

    public ErrorMessage(int error, String message) {
        this.error = error;
        this.message = message;
    }

    public int getError() {
        return this.error;
    }

    public void setError(int error) {
        this.error = error;
    }

    public String getMessage() {
        return this.message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
