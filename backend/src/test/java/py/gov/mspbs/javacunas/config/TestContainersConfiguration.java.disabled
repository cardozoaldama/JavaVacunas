package py.gov.mspbs.javacunas.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.OracleContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * TestContainers configuration for integration tests with Oracle Database.
 * This configuration provides a real Oracle database instance for integration tests.
 */
@TestConfiguration(proxyBeanMethods = false)
public class TestContainersConfiguration {

    /**
     * Creates an Oracle Container for integration testing.
     * The container is automatically started and stopped by TestContainers.
     * Spring Boot auto-configures the datasource from this container.
     *
     * @return OracleContainer configured with Oracle XE 21c
     */
    @Bean
    @ServiceConnection
    OracleContainer oracleContainer() {
        return new OracleContainer(DockerImageName.parse("gvenzl/oracle-xe:21-slim-faststart"))
                .withUsername("javacunas_test")
                .withPassword("test_password")
                .withReuse(true);
    }
}
