package py.gov.mspbs.javacunas;

import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import py.gov.mspbs.javacunas.config.TestContainersConfiguration;

/**
 * Base class for integration tests.
 * Loads full Spring context with TestContainers Oracle database.
 * Slower than unit tests but tests real database interactions.
 * Integration test classes should end with 'IT' suffix.
 */
@SpringBootTest
@Import(TestContainersConfiguration.class)
@ActiveProfiles("integration-test")
@Transactional
public abstract class BaseIT {

    @Autowired
    protected JdbcTemplate jdbcTemplate;

    /**
     * Clean up database after each test to ensure test isolation.
     * This method is called after each test method.
     */
    @AfterEach
    void cleanUp() {
        // Clean up tables in reverse order of dependencies
        jdbcTemplate.execute("DELETE FROM vaccination_records");
        jdbcTemplate.execute("DELETE FROM appointments");
        jdbcTemplate.execute("DELETE FROM vaccine_inventory");
        jdbcTemplate.execute("DELETE FROM vaccination_schedules");
        jdbcTemplate.execute("DELETE FROM child_guardians");
        jdbcTemplate.execute("DELETE FROM guardians");
        jdbcTemplate.execute("DELETE FROM children");
        jdbcTemplate.execute("DELETE FROM vaccines");
        jdbcTemplate.execute("DELETE FROM notifications");
        jdbcTemplate.execute("DELETE FROM users");
        jdbcTemplate.execute("DELETE FROM audit_log");
    }
}
