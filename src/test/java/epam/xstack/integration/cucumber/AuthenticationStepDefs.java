package epam.xstack.integration.cucumber;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import epam.xstack.model.Trainee;
import epam.xstack.model.User;
import epam.xstack.repository.UserRepository;
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

public class AuthenticationStepDefs {
    @Autowired
    TestRestTemplate restTemplate;

    @Autowired
    UserRepository userRepository;

    @LocalServerPort
    private int port;
    private String baseUrl;
    private String jwtToken;
    HttpEntity<Map<String, String>> request;
    ResponseEntity<String> response;
    ResponseEntity<Trainee> traineeResponse;

    @Given("request to log in with correct credentials")
    public void request_to_log_in_with_correct_credentials() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, String> jsonMap = Map.of(
                "username", "bob",
                "password", "password2");

        request = new HttpEntity<>(jsonMap, headers);
    }

    @When("app receives the login request")
    public void app_receives_the_login_request() throws URISyntaxException {
        baseUrl = "http://localhost:" + port + "/api/v1/auth";

        response = restTemplate.exchange(
                new URI(baseUrl), HttpMethod.POST, request, String.class);
    }

    @Then("user gets token in response")
    public void user_gets_token_in_response() {
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("jwt-token");
    }

    @Given("request to log in with incorrect credentials")
    public void request_to_log_in_with_incorrect_credentials() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, String> jsonMap = Map.of(
                "username", "bob",
                "password", "wrongpass");

        request = new HttpEntity<>(jsonMap, headers);
    }

    @Then("user doesn't get auth token")
    public void user_doesn_t_get_auth_token() {
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNull();
    }

    @Given("request to change password with correct credentials")
    public void request_to_change_password_with_correct_credentials() throws URISyntaxException, JsonProcessingException {
        jwtToken = getJwtToken();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(jwtToken);

        Map<String, String> jsonMap = Map.of(
                "username", "bob",
                "oldPassword", "password2",
                "newPassword", "password22");

        request = new HttpEntity<>(jsonMap, headers);
    }

    @When("app receives the change password request")
    public void app_receives_the_change_password_request() throws URISyntaxException {
        baseUrl = "http://localhost:" + port + "/api/v1/auth";

        response = restTemplate.exchange(
                new URI(baseUrl + "/2/password"), HttpMethod.PUT, request, String.class);
    }

    @Then("user's password is successfully changed")
    public void user_s_password_is_successfully_changed() {
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        Optional<User> userOpt = userRepository.findByUsername(request.getBody().get("username"));
        String newPassword = request.getBody().get("newPassword");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(userOpt).isPresent();

        User user = userOpt.get();
        assertThat(passwordEncoder.matches(newPassword, user.getPassword())).isTrue();
    }

    @Given("request to change password with empty fields")
    public void request_to_change_password_with_empty_fields() throws URISyntaxException, JsonProcessingException {
        jwtToken = getJwtToken();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(jwtToken);

        Map<String, String> jsonMap = Map.of(
                "username", "",
                "oldPassword", "",
                "newPassword", "");

        request = new HttpEntity<>(jsonMap, headers);
    }

    @Then("response has error description")
    public void response_has_error_description() {
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(response.getBody())
                .contains("Password cannot be empty")
                .contains("Username cannot be empty")
                .contains("New password cannot be empty");
    }

    @Given("request to change password with incorrect creds")
    public void request_to_change_password_with_incorrect_creds() throws URISyntaxException, JsonProcessingException {
        jwtToken = getJwtToken();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(jwtToken);

        Map<String, String> jsonMap = Map.of(
                "username", "bob",
                "oldPassword", "wrongpassword",
                "newPassword", "password22");

        request = new HttpEntity<>(jsonMap, headers);
    }

    @Then("user's password is not changed")
    public void user_s_password_is_not_changed() {
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        Optional<User> userOpt = userRepository.findByUsername(request.getBody().get("username"));
        String newPassword = request.getBody().get("newPassword");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(userOpt).isPresent();

        User user = userOpt.get();
        assertThat(passwordEncoder.matches(newPassword, user.getPassword())).isFalse();
    }

    @Given("request to access account info with correct credentials")
    public void request_to_access_account_info_with_correct_credentials() throws URISyntaxException, JsonProcessingException {
        jwtToken = getJwtToken();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(jwtToken);

        request = new HttpEntity<>(headers);
    }

    @When("app receives the get info request")
    public void app_receives_the_get_info_request() throws URISyntaxException {
        baseUrl = "http://localhost:" + port + "/api/v1/trainees";

        traineeResponse = restTemplate.exchange(
                new URI(baseUrl + "/2"), HttpMethod.GET, request, Trainee.class);
    }

    @Then("response has user account info")
    public void response_has_user_account_info() {
        Trainee trainee = traineeResponse.getBody();
        assertThat(traineeResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        assertThat(trainee.getFirstName()).isEqualTo("Bob");
        assertThat(trainee.getLastName()).isEqualTo("Smith");
        assertThat(trainee.getAddress()).isEqualTo("456 Elm St");
        assertThat(trainee.getDateOfBirth()).isEqualTo("1985-05-20");
        assertThat(trainee.getActive()).isTrue();
        assertThat(trainee.getTrainers()).isNotEmpty();

        assertThat(trainee.getUsername()).isNull();
        assertThat(trainee.getPassword()).isNull();
        assertThat(trainee.getTrainingList()).isNull();
        assertThat(trainee.getId()).isEqualTo(0);
    }

    @When("app receives the get another account info request")
    public void app_receives_the_get_another_account_info_request() throws URISyntaxException {
        baseUrl = "http://localhost:" + port + "/api/v1/trainees";

        traineeResponse = restTemplate.exchange(
                new URI(baseUrl + "/3"), HttpMethod.GET, request, Trainee.class);
    }

    @Then("response has forbidden error")
    public void response_has_forbidden_error() {
        assertThat(traineeResponse.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(traineeResponse.getBody()).isNull();
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
