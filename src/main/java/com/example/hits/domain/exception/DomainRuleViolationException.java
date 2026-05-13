package com.example.hits.domain.exception;

public class DomainRuleViolationException extends RuntimeException {

    public DomainRuleViolationException(String message) {
        super(message);
    }
}
