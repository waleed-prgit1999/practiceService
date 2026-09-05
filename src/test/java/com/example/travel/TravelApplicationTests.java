package com.example.travel;

import com.example.travel.support.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;

class TravelApplicationTests extends AbstractIntegrationTest {

    @Test
    void contextLoads() {
        // Verifies the application context starts and Flyway migrations apply cleanly
        // against a real MySQL instance (see AbstractIntegrationTest).
    }
}
