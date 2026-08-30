package com.tracker.habit.exception;

public class ApiExceptions {
    public static class ResourceNotFoundException extends RuntimeException {
        public ResourceNotFoundException(String message) {
            super(message);
        }
    }

    public static class AlreadyCompletedException extends RuntimeException {
        public AlreadyCompletedException(String message) {
            super(message);
        }
    }
}
