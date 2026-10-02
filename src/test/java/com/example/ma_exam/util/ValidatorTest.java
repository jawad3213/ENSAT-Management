package com.example.ma_exam.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ValidatorTest {

    @Test
    void validFormHasNoErrors() {
        Validator v = new Validator()
                .required("Code", "INFO")
                .maxLength("Code", "INFO", 50)
                .email("Email", "jean.dupont@ensat.ac.ma")
                .required("Filière", new Object());
        assertTrue(v.isValid());
        assertEquals("", v.getMessage());
    }

    @Test
    void blankAndNullValuesAreRejected() {
        Validator v = new Validator()
                .required("Code", "")
                .required("Nom", "   ")
                .required("Intitulé", (String) null)
                .required("Filière", (Object) null);
        assertFalse(v.isValid());
        assertEquals(4, v.getErrors().size());
        assertTrue(v.getMessage().contains("« Code »"));
    }

    @Test
    void tooLongValueIsRejected() {
        Validator v = new Validator().maxLength("Code", "x".repeat(51), 50);
        assertFalse(v.isValid());
        assertTrue(v.getMessage().contains("50"));
    }

    @Test
    void invalidEmailsAreRejected() {
        for (String email : new String[]{"jean", "jean@", "@ensat.ma", "jean@ensat", "jean dupont@ensat.ma"}) {
            assertFalse(new Validator().email("Email", email).isValid(), email);
        }
    }

    @Test
    void emailCheckIgnoresEmptyValue() {
        // Emptiness is the job of required(), not email()
        assertTrue(new Validator().email("Email", "").isValid());
    }

    @Test
    void cleanTrimsInput() {
        assertEquals("INFO", Validator.clean("  INFO \t"));
        assertNull(Validator.clean(null));
    }
}
