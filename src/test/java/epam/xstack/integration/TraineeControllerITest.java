package epam.xstack.integration;

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

    @BeforeAll
    void setup() {
        baseUrl = "http://localhost:" + port;
    }

    @Test
    void testCreateTrainee_ReturnsCreatedResponseEntity() throws URISyntaxException {
        String firstName = "Jason";
        String lastName = "Posh";
        String dateOfBirth = "2002-03-04";
        String address = "London, Duke st. 3";
        String expectedUsername = firstName.toLowerCase().concat(".").concat(lastName.toLowerCase());

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

            Trainee trainee = session.get(Trainee.class, id);
            assertThat(trainee.getUsername()).isEqualTo(expectedUsername);
            assertThat(trainee.getPassword()).isEqualTo(response.getBody().getPassword());
            assertThat(trainee.getFirstName()).isEqualTo(firstName);
            assertThat(trainee.getLastName()).isEqualTo(lastName);
            assertThat(trainee.getDateOfBirth()).isEqualTo(dateOfBirth);
            assertThat(trainee.getAddress()).isEqualTo(address);
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
        String expectedIsActive = "false";
        String expectedAddress = "456 Elm St UPD";
        String expectedDoB = "1986-06-21";
        Trainee expectedTrainee = new Trainee(expectedFirstName, expectedLastName, "bob", "password2",
                Boolean.parseBoolean(expectedIsActive), LocalDate.parse(expectedDoB), expectedAddress);

        // payload
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, String> jsonMap = Map.of(
                "username", "bob",
                "password", "password2",
                "firstName", expectedFirstName,
                "lastName", expectedLastName,
                "isActive", expectedIsActive,
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
        assertThat(actual.getIsActive()).isEqualTo(expected.getIsActive());
        assertThat(actual.getAddress()).isEqualTo(expected.getAddress());
        assertThat(actual.getDateOfBirth()).isEqualTo(expected.getDateOfBirth());
    }
}
