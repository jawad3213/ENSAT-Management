package com.example.ma_exam.util;

/**
 * Thrown when an operation would break a business rule (e.g. enrolling a suspended student).
 * Its message is meant to be shown to the user as is.
 */
public class BusinessRuleException extends Exception {
    public BusinessRuleException(String message) {
        super(message);
    }
}
