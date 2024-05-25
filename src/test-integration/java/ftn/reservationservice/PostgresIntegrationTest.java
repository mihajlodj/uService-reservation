package ftn.reservationservice;

import org.junit.ClassRule;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

@Transactional
@Testcontainers
public class PostgresIntegrationTest {

    @ClassRule
    public static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:12.6-alpine")
            .withDatabaseName("devops_reservation_db")
            .withUsername("devops_ftn")
            .withPassword("devops_ftn");

    @DynamicPropertySource
    static void registerPgProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> "jdbc:tc:postgresql:12.6:///devops_reservation_db");
        registry.add("spring.datasource.username", () -> "devops_ftn");
        registry.add("spring.datasource.password", () -> "devops_ftn");
        registry.add("spring.datasource.hikari.maximumPoolSize", () -> 50);
        registry.add("spring.datasource.hikari.minimumIdle", () -> 5);
    }

}
