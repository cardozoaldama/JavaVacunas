package py.gov.mspbs.javacunas;

import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.oracle.OracleContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Abstract base class for integration tests using Oracle Database 23c Free.
 *
 * This class provides:
 * - TestContainers 2.0+ with Docker 29.x API compatibility
 * - Oracle 23c Free database with gvenzl/oracle-free image
 * - @ServiceConnection for automatic DataSource configuration (Spring Boot 3.2+)
 * - Container reuse for improved test performance
 * - Proper database cleanup after each test
 *
 * Integration tests should extend this class and use the *IT.java naming convention.
 *
 * Breaking changes from TestContainers 1.21.3 to 2.0.2:
 * - Updated Docker API compatibility (1.32 -> 1.47)
 * - New module structure: org.testcontainers:oracle-free replaces oracle-xe
 * - @ServiceConnection annotation replaces manual DataSource configuration
 * - Container.withReuse() now requires testcontainers.reuse.enable=true in ~/.testcontainers.properties
 */
@SpringBootTest
@Testcontainers
@ActiveProfiles("integration-test")
@Transactional
public abstract class AbstractOracleIntegrationTest {

    /**
     * Oracle 23c Free container using gvenzl/oracle-free image.
     *
     * Features:
     * - Fast startup with -slim-faststart variant (30-60 seconds)
     * - Free to use, no license restrictions
     * - Compatible with Oracle 23c features
     *
     * Container reuse strategy:
     * - Reuse is enabled for performance (avoids container restart between test classes)
     * - Requires testcontainers.reuse.enable=true in ~/.testcontainers.properties
     * - Database is cleaned between tests via @AfterEach cleanup method
     * - Shared container reduces test suite execution time significantly
     */
    @Container
    @ServiceConnection
    static OracleContainer oracleContainer = new OracleContainer(
            DockerImageName.parse("gvenzl/oracle-free:23.3-slim-faststart")
    )
    .withReuse(true)
    .withStartupTimeout(java.time.Duration.ofMinutes(5)); // First-time DB init can take 3-4 minutes

    @Autowired
    protected JdbcTemplate jdbcTemplate;

    /**
     * Clean up database after each test to ensure test isolation.
     *
     * This method deletes data in reverse order of foreign key dependencies
     * to avoid constraint violations. The order matters because:
     * 1. Child tables (with foreign keys) must be cleaned before parent tables
     * 2. Junction tables must be cleaned before their referenced entities
     *
     * Note: This is more efficient than @Transactional rollback when using
     * container reuse, as it maintains the schema and allows Flyway migrations
     * to run only once per container lifecycle.
     */
    @AfterEach
    void cleanUpDatabase() {
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

    /**
     * Helper method to verify database connection and Oracle version.
     * Useful for debugging test setup issues.
     */
    protected String getOracleVersion() {
        return jdbcTemplate.queryForObject(
            "SELECT banner FROM v$version WHERE banner LIKE 'Oracle%'",
            String.class
        );
    }

    /**
     * Helper method to check if a table exists.
     * Useful for verifying Flyway migrations ran successfully.
     */
    protected boolean tableExists(String tableName) {
        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM user_tables WHERE table_name = UPPER(?)",
            Integer.class,
            tableName
        );
        return count != null && count > 0;
    }

    /**
     * Helper method to count rows in a table.
     * Useful for asserting database state in tests.
     */
    protected int countRowsInTable(String tableName) {
        return jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM " + tableName,
            Integer.class
        );
    }
}
