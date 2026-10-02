package com.example.ma_exam.util;

import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

class AlertsTest {

    @Test
    void businessRuleMessageIsShownAsIs() {
        assertEquals("Un élève suspendu ne peut pas être inscrit à un cours.",
                Alerts.describe(new BusinessRuleException("Un élève suspendu ne peut pas être inscrit à un cours.")));
    }

    @Test
    void uniqueViolationIsExplained() {
        String message = Alerts.describe(new SQLException("duplicate key", "23505"));
        assertTrue(message.startsWith("Cette valeur existe déjà"));
        assertTrue(message.contains("duplicate key"));
    }

    @Test
    void connectionFailureMentionsConfiguration() {
        assertTrue(Alerts.describe(new SQLException("Connection refused", "08001")).contains("config.properties"));
        assertTrue(Alerts.describe(new SQLException("password authentication failed", "28P01")).contains("db.password"));
    }

    @Test
    void sqlExceptionWithoutStateFallsBackToGenericMessage() {
        assertTrue(Alerts.describe(new SQLException("boom")).contains("boom"));
    }
}
