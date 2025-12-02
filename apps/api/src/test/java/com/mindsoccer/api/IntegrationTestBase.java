package com.mindsoccer.api;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Base class for integration tests.
 * Uses docker-compose services (PostgreSQL, Redis) running on localhost.
 *
 * Before running tests, ensure docker-compose is started:
 * docker-compose -f infra/docker-compose.yml up -d
 */
@SpringBootTest
@ActiveProfiles("test")
public abstract class IntegrationTestBase {
    // Configuration is provided via application-test.yml
}
