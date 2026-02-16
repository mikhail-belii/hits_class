package com.example.hits.application.handler;

import lombok.Getter;

import java.util.Dictionary;
import java.util.Hashtable;

@Getter
public class ExceptionWrapper extends Exception {
    private final Class<? extends Exception> exceptionClass;
    private final Dictionary<String, String> errors = new Hashtable<>();

    public ExceptionWrapper(Exception originalException) {
        this.exceptionClass = originalException.getClass();
    }

    public void addError(String reason, String error){
        errors.put(reason, error);
    }

    public boolean hasErrors(){
        return !errors.isEmpty();
    }
}
