package epam.xstack.integration.cucumber;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import epam.xstack.dto.auth.AuthDTO;
import epam.xstack.model.Trainee;
import epam.xstack.repository.TraineeRepository;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
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
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class TraineeStepDefs {
    @Autowired
    TestRestTemplate restTemplate;

    @Autowired
    TraineeRepository traineeRepository;

    @LocalServerPort
    private int port;
    private String baseUrl;
    private String jwtToken;
    HttpEntity<Map<String, String>> request;
    ResponseEntity<AuthDTO> authResponse;
    ResponseEntity<String> errorResponse;
    ResponseEntity<Trainee> traineeResponse;

    @Given("correct request to create new trainee")
    public void correct_request_to_create_new_trainee() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, String> jsonMap = Map.of(
                "firstName", "Jason",
                "lastName", "Posh",
                "dateOfBirth", "2002-03-04",
                "address", "London, Duke st. 3");

        request = new HttpEntity<>(jsonMap, headers);
    }

    @When("app receives the create trainee request")
    public void app_receives_the_create_trainee_request() throws URISyntaxException {
        baseUrl = "http://localhost:" + port + "/api/v1/trainees";

        authResponse = restTemplate.postForEntity(
                new URI(baseUrl), request, AuthDTO.class);
    }

    @Then("user gets credentials in response")
    public void user_gets_credentials_in_response() {
        assertThat(authResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(authResponse.getBody().getUsername()).isEqualTo("jason.posh");
        assertThat(authResponse.getBody().getPassword()).hasSize(10);
        assertThat(authResponse.getHeaders().getLocation().toString()).matches(".*/api/v1/trainees/\\d+$");
    }

    @Then("new trainer account is created")
    public void new_trainer_account_is_created() {
        Map<String, String> requestBody = request.getBody();
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        long id = Long.parseLong(
                authResponse.getHeaders().getLocation().getPath()
                        .replace("/api/v1/trainees/", ""));

        Optional<Trainee> traineeOpt = traineeRepository.findById(id);
        assertThat(traineeOpt).isPresent();

        Trainee actual = traineeOpt.get();

        assertThat(actual.getUsername()).isEqualTo("jason.posh");
        assertThat(passwordEncoder.matches(
                authResponse.getBody().getPassword(), actual.getPassword())).isTrue();
        assertThat(actual.getFirstName()).isEqualTo(requestBody.get("firstName"));
        assertThat(actual.getLastName()).isEqualTo(requestBody.get("lastName"));
        assertThat(actual.getDateOfBirth()).isEqualTo(requestBody.get("dateOfBirth"));
        assertThat(actual.getAddress()).isEqualTo(requestBody.get("address"));
    }

    @Given("request to create new trainee with empty fields")
    public void request_to_create_new_trainee_with_empty_fields() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, String> jsonMap = Map.of(
                "firstName", "",
                "lastName", "",
                "dateOfBirth", "",
                "address", "");

        request = new HttpEntity<>(jsonMap, headers);
    }

    @When("app receives the wrong create trainee request")
    public void app_receives_the_wrong_create_trainee_request() throws URISyntaxException {
        baseUrl = "http://localhost:" + port + "/api/v1/trainees";

        errorResponse = restTemplate.postForEntity(new URI(baseUrl), request, String.class);
    }

    @Then("response has trainee request error description")
    public void response_has_trainee_request_error_description() {
        assertThat(errorResponse.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(errorResponse.getBody())
                .contains("First name cannot be empty")
                .contains("Last name cannot be empty");
    }

    @Given("correct request to update existing trainee")
    public void correct_request_to_update_existing_trainee() throws URISyntaxException, JsonProcessingException {
        jwtToken = getJwtToken();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(jwtToken);

        Map<String, String> jsonMap = Map.of(
                "username", "bob",
                "password", "password2",
                "firstName", "BobUPD",
                "lastName", "SmithUPD",
                "active", "false",
                "address", "456 Elm St UPD",
                "dateOfBirth", "1986-06-21");

        request = new HttpEntity<>(jsonMap, headers);
    }
    @When("app receives the update trainee request")
    public void app_receives_the_update_trainee_request() throws URISyntaxException {
        baseUrl = "http://localhost:" + port + "/api/v1/trainees/2";

        traineeResponse = restTemplate.exchange(
                new URI(baseUrl), HttpMethod.PUT, request, Trainee.class);
    }
    @Then("existing trainee account is updated")
    public void existing_trainee_account_is_updated() {
        Trainee actual = traineeResponse.getBody();
        Optional<Trainee> existingTraineeOpt = traineeRepository.findByUsername(actual.getUsername());
        assertThat(existingTraineeOpt).isPresent();
        assertThat(actual)
                .usingRecursiveComparison()
                .ignoringFields("trainingList", "trainers", "id", "password")
                .isEqualTo(existingTraineeOpt.get());

    }
    @Then("user gets updated trainee account in response")
    public void user_gets_updated_trainee_account_in_response() {
        Map<String, String> requestBody = request.getBody();
        Trainee actual = traineeResponse.getBody();

        assertThat(traineeResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        assertThat(actual.getFirstName()).isEqualTo(requestBody.get("firstName"));
        assertThat(actual.getLastName()).isEqualTo(requestBody.get("lastName"));
        assertThat(actual.getActive()).isEqualTo(Boolean.parseBoolean(requestBody.get("active")));
        assertThat(actual.getAddress()).isEqualTo(requestBody.get("address"));
        assertThat(actual.getDateOfBirth()).isEqualTo(requestBody.get("dateOfBirth"));
    }

    private String getJwtToken() throws URISyntaxException, JsonProcessingException {
        if (jwtToken != null) {
            return jwtToken;
        } else {
            baseUrl = "http://localhost:" + port + "/api/v1/auth";

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
}
