package epam.xstack.integration.cucumber;

import io.cucumber.spring.CucumberContextConfiguration;
import io.zonky.test.db.AutoConfigureEmbeddedDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@CucumberContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("stg")
@AutoConfigureEmbeddedDatabase(refresh = AutoConfigureEmbeddedDatabase.RefreshMode.AFTER_EACH_TEST_METHOD)
public class CucumberConfig {
}
