package ca.gc.aafc.objectstore.api;

import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import ca.gc.aafc.objectstore.api.testsupport.DBBackedIntegrationTest;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = MainConfiguration.class)
@Transactional
@ActiveProfiles("test")
public abstract class BaseIntegrationTest extends DBBackedIntegrationTest {

  
}
