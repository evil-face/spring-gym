package epam.xstack.integration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import epam.xstack.dto.auth.AuthDTO;
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

import javax.persistence.EntityManager;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("stg")
@AutoConfigureEmbeddedDatabase(refresh = AutoConfigureEmbeddedDatabase.RefreshMode.AFTER_EACH_TEST_METHOD)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
final class TraineeControllerITest {
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
        baseUrl = "http://localhost:" + port;
        jwtToken = getJwtToken();
    }

    @Test
    void testCreateTrainee_ReturnsCreatedResponseEntity() throws URISyntaxException {
        String firstName = "Jason";
        String lastName = "Posh";
        String dateOfBirth = "2002-03-04";
        String address = "London, Duke st. 3";
        String expectedUsername = "jason.posh";
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

        // payload
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, String> jsonMap = Map.of(
                "firstName", firstName,
                "lastName", lastName,
                "dateOfBirth", dateOfBirth,
                "address", address);
        HttpEntity<Map<String, String>> request = new HttpEntity<>(jsonMap, headers);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<AuthDTO> response = restTemplate.postForEntity(
                new URI(baseUrl + "/api/v1/trainees"), request, AuthDTO.class);

        // check response
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().getUsername()).isEqualTo(expectedUsername);
        assertThat(response.getBody().getPassword()).hasSize(10);
        assertThat(response.getHeaders().getLocation().toString()).matches(".*/api/v1/trainees/\\d+$");

        // check DB
        try (Session session = entityManager.unwrap(Session.class)) {
            long id = Long.parseLong(
                    response.getHeaders().getLocation().getPath()
                            .replace("/api/v1/trainees/", ""));

            Trainee actual = session.get(Trainee.class, id);
            assertThat(actual.getUsername()).isEqualTo(expectedUsername);
            assertThat(passwordEncoder.matches(response.getBody().getPassword(), actual.getPassword())).isTrue();
            assertThat(actual.getFirstName()).isEqualTo(firstName);
            assertThat(actual.getLastName()).isEqualTo(lastName);
            assertThat(actual.getDateOfBirth()).isEqualTo(dateOfBirth);
            assertThat(actual.getAddress()).isEqualTo(address);
        }
    }

    @Test
    void testCreateTrainee_ReturnsUnprocessableEntity() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, String> jsonMap = Map.of(
                "firstName", "",
                "lastName", "",
                "dateOfBirth", "",
                "address", "");
        HttpEntity<Map<String, String>> request = new HttpEntity<>(jsonMap, headers);

        RestTemplate restTemplate = new RestTemplate();

        HttpClientErrorException exception = Assertions.assertThrows(HttpClientErrorException.class, () -> {
            restTemplate.postForEntity(new URI(baseUrl + "/api/v1/trainees"), request, AuthDTO.class);
        });

        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(exception.getResponseBodyAsString())
                .contains("First name cannot be empty")
                .contains("Last name cannot be empty");
    }

    @Test
    void testUpdateTrainee_ReturnsUpdatedTraineeResponseEntity() throws URISyntaxException {
        String expectedFirstName = "BobUPD";
        String expectedLastName = "SmithUPD";
        String expectedActive = "false";
        String expectedAddress = "456 Elm St UPD";
        String expectedDoB = "1986-06-21";
        Trainee expectedTrainee = new Trainee(expectedFirstName, expectedLastName, "bob", "password2",
                Boolean.parseBoolean(expectedActive), LocalDate.parse(expectedDoB), expectedAddress);

        // payload
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(jwtToken);

        Map<String, String> jsonMap = Map.of(
                "username", "bob",
                "password", "password2",
                "firstName", expectedFirstName,
                "lastName", expectedLastName,
                "active", expectedActive,
                "address", expectedAddress,
                "dateOfBirth", expectedDoB);
        HttpEntity<Map<String, String>> request = new HttpEntity<>(jsonMap, headers);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<Trainee> response = restTemplate.exchange(
                new URI(baseUrl + "/api/v1/trainees/2"), HttpMethod.PUT, request, Trainee.class);

        // check response
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Trainee actualResponse = response.getBody();

        areFieldsEqual(actualResponse, expectedTrainee);

        // check DB
        try (Session session = entityManager.unwrap(Session.class)) {
            Trainee actualDB = session.get(Trainee.class, 2L);

            areFieldsEqual(actualDB, expectedTrainee);
        }
    }

    private void areFieldsEqual(Trainee actual, Trainee expected) {
        assertThat(actual.getFirstName()).isEqualTo(expected.getFirstName());
        assertThat(actual.getLastName()).isEqualTo(expected.getLastName());
        assertThat(actual.getActive()).isEqualTo(expected.getActive());
        assertThat(actual.getAddress()).isEqualTo(expected.getAddress());
        assertThat(actual.getDateOfBirth()).isEqualTo(expected.getDateOfBirth());
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
                new URI(baseUrl + "/api/v1/auth"), HttpMethod.POST, request, String.class);

        String responseBody = responseEntity.getBody();
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, String> responseMap =
                objectMapper.readValue(responseBody, new TypeReference<Map<String, String>>() {});

        return responseMap.get("jwt-token");
    }
}
