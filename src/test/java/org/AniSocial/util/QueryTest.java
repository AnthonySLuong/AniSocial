package org.AniSocial.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class QueryTest {
    Variable variable;

    @BeforeEach
    void setUp() {
        this.variable = new Variable(new int[] {12345, 54321, 67890, 98765});
    }

    @Test
    @DisplayName("PayLoad Test")
    void payLoadTest() {
        String variableJson = this.variable.toJson().toString();
        String json = String.format("{\"variables\":%s,\"query\":\"%s\"}", variableJson, Query.QUERY);
        assertEquals(json, Query.toPayload(this.variable).toString());
    }
}
