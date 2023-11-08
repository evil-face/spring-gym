package epam.xstack.integration;

import epam.xstack.model.Trainee;
import io.zonky.test.db.AutoConfigureEmbeddedDatabase;
import org.hibernate.Session;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;

import javax.persistence.EntityManager;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("stg")
@AutoConfigureEmbeddedDatabase(refresh = AutoConfigureEmbeddedDatabase.RefreshMode.AFTER_EACH_TEST_METHOD)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
final class AuthControllerITest {
    @Autowired
    TestRestTemplate restTemplate;
    @Autowired
    EntityManager entityManager;

    @LocalServerPort
    private int port;
    private String baseUrl;

    @BeforeAll
    void setup() {
        baseUrl = "http://localhost:" + port;
    }

    // todo find out if we can send request GET+json payload in RestTemplate

    @Test
    void testChangePassword_ReturnsOkResponseEntity() throws URISyntaxException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String newPassword = "password22";

        Map<String, String> jsonMap = Map.of(
                "username", "bob",
                "oldPassword", "password2",
                "newPassword", newPassword);
        HttpEntity<Map<String, String>> request = new HttpEntity<>(jsonMap, headers);

        RestTemplate restTemplate = new RestTemplate();

        ResponseEntity<String> responseEntity = restTemplate.exchange(
                new URI(baseUrl + "/api/v1/auth/2"), HttpMethod.PUT, request, String.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);

        try (Session session = entityManager.unwrap(Session.class)) {
            Trainee trainee = session.get(Trainee.class, 2L);
            assertThat(trainee.getPassword()).isEqualTo(newPassword);
        }
    }

    @Test
    void testChangePassword_ReturnsUnprocessableEntity() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, String> jsonMap = Map.of(
                "username", "",
                "oldPassword", "",
                "newPassword", "");
        HttpEntity<Map<String, String>> request = new HttpEntity<>(jsonMap, headers);

        RestTemplate restTemplate = new RestTemplate();

        HttpClientErrorException exception = Assertions.assertThrows(HttpClientErrorException.class, () -> {
            restTemplate.exchange(new URI(baseUrl + "/api/v1/auth/2"), HttpMethod.PUT, request, String.class);
        });

        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(exception.getResponseBodyAsString())
                .contains("Password cannot be empty")
                .contains("Username cannot be empty")
                .contains("New password cannot be empty");
    }
}
