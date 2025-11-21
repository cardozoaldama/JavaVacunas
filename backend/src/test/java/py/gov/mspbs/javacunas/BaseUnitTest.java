package py.gov.mspbs.javacunas;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

/**
 * Base class for unit tests.
 * Uses Mockito for mocking dependencies.
 * Does not load Spring context (fast tests).
 * Uses H2 in-memory database when database is needed.
 */
@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
public abstract class BaseUnitTest {
}
