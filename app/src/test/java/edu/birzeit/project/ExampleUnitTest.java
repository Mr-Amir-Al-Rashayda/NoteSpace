package edu.birzeit.project;

import org.junit.Test;

import edu.birzeit.project.utils.ValidationUtils;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ExampleUnitTest {
    @Test
    public void namesRequireThreeToTenTrimmedCharacters() {
        assertFalse(ValidationUtils.isValidName("Al"));
        assertTrue(ValidationUtils.isValidName("  Alice  "));
        assertFalse(ValidationUtils.isValidName("Christopher"));
    }

    @Test
    public void passwordRequiresLengthAndAllCharacterGroups() {
        assertFalse(ValidationUtils.isValidPassword("Aa1aa"));
        assertFalse(ValidationUtils.isValidPassword("AAAAAAAA1"));
        assertFalse(ValidationUtils.isValidPassword("aaaaaaaa1"));
        assertFalse(ValidationUtils.isValidPassword("Abcdefghijk12"));
        assertTrue(ValidationUtils.isValidPassword("Study1"));
    }
}
