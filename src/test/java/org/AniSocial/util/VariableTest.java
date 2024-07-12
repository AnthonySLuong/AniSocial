package org.AniSocial.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

public class VariableTest {
    Variable variable;
    int[] userid;
    long seconds;

    @BeforeEach
    void setUp() {
        this.userid = new int[] {12345, 54321, 67890, 98765};
        this.seconds = (System.currentTimeMillis() / 1000) - 15;
        this.variable = new Variable(userid);
    }

    @Test
    @DisplayName("Constructor Test")
    void ConstructorTest() {
        assertThrows(IllegalArgumentException.class, () -> new Variable(null));
        assertArrayEquals(new int[] {12345, 54321, 67890, 98765}, this.variable.getIds());
        assertEquals(1, this.variable.getPages());
        assertEquals(this.seconds, this.variable.getSeconds());
    }

    @Test
    @DisplayName("Increase Paging")
    void IncreasePagingTest() {
        assertEquals(1, this.variable.getPages());
        this.variable.increasePage();
        assertEquals(2, this.variable.getPages());
        this.variable.increasePage();
        assertEquals(3, this.variable.getPages());
    }

    @Test
    @DisplayName("JSON Test")
    void JSONTest() {
        String json = String.format("{\"seconds\":%d,\"pages\":%d,\"ids\":%s}", this.seconds, this.variable.getPages(), Arrays.toString(this.userid));
        assertEquals(json.replace(" ", ""), this.variable.toJson().toString());
        this.variable.increasePage();
        json = String.format("{\"seconds\":%d,\"pages\":%d,\"ids\":%s}", this.seconds, this.variable.getPages(), Arrays.toString(this.userid));
        assertEquals(json.replace(" ", ""), this.variable.toJson().toString());
    }
}
