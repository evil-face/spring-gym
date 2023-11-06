package epam.xstack.unit.service;

import epam.xstack.service.LoginAttemptService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = LoginAttemptService.class)
@TestPropertySource(properties = {
        "bruteforce.protection.max-attempts=3",
        "bruteforce.protection.cache-size=100",
        "bruteforce.protection.block-period-minutes=5"})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class LoginAttemptServiceTest {
    @Autowired
    LoginAttemptService loginAttemptService;
    String USERNAME = "user";

    @Test
    void testHandleFailedLoginAttempt_userNotBlocked() {
        loginAttemptService.handleFailedLoginAttempt("1234", USERNAME);

        assertThat(loginAttemptService.isUserBlocked(USERNAME)).isFalse();
    }

    @Test
    void testHandleFailedLoginAttempt_userIsBlocked() {
        loginAttemptService.handleFailedLoginAttempt("1234", USERNAME);
        loginAttemptService.handleFailedLoginAttempt("1234", USERNAME);
        loginAttemptService.handleFailedLoginAttempt("1234", USERNAME);

        assertThat(loginAttemptService.isUserBlocked(USERNAME)).isTrue();
    }
}
