package epam.xstack.integration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
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
    private String jwtToken;

    @BeforeAll
    void setup() throws URISyntaxException, JsonProcessingException {
        baseUrl = "http://localhost:" + port + "/api/v1/auth";
        jwtToken = getJwtToken();
    }

    @Test
    void testChangePassword_ReturnsOkResponseEntity() throws URISyntaxException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(jwtToken);
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

        String newPassword = "password22";

        Map<String, String> jsonMap = Map.of(
                "username", "bob",
                "oldPassword", "password2",
                "newPassword", newPassword);
        HttpEntity<Map<String, String>> request = new HttpEntity<>(jsonMap, headers);

        RestTemplate restTemplate = new RestTemplate();

        ResponseEntity<String> responseEntity = restTemplate.exchange(
                new URI(baseUrl + "/2/password"), HttpMethod.PUT, request, String.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);

        try (Session session = entityManager.unwrap(Session.class)) {
            Trainee trainee = session.get(Trainee.class, 2L);
            assertThat(passwordEncoder.matches(newPassword, trainee.getPassword())).isTrue();
        }
    }

    @Test
    void testChangePassword_ReturnsUnprocessableEntity() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(jwtToken);

        Map<String, String> jsonMap = Map.of(
                "username", "",
                "oldPassword", "",
                "newPassword", "");
        HttpEntity<Map<String, String>> request = new HttpEntity<>(jsonMap, headers);

        RestTemplate restTemplate = new RestTemplate();

        HttpClientErrorException exception = Assertions.assertThrows(HttpClientErrorException.class, () -> {
            restTemplate.exchange(
                    new URI(baseUrl + "/2/password"), HttpMethod.PUT, request, String.class);
        });

        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(exception.getResponseBodyAsString())
                .contains("Password cannot be empty")
                .contains("Username cannot be empty")
                .contains("New password cannot be empty");
    }

    private String getJwtToken() throws URISyntaxException, JsonProcessingException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, String> jsonMap = Map.of(
                "username", "bob",
                "password", "password2");
        HttpEntity<Map<String, String>> request = new HttpEntity<>(jsonMap, headers);

        RestTemplate restTemplate = new RestTemplate();

        ResponseEntity<String> responseEntity = restTemplate.exchange(
                new URI(baseUrl), HttpMethod.POST, request, String.class);

        String responseBody = responseEntity.getBody();
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, String> responseMap =
                objectMapper.readValue(responseBody, new TypeReference<Map<String, String>>() {});

        return responseMap.get("jwt-token");
    }
}
