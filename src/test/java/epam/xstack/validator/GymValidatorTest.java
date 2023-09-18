package epam.xstack.validator;

import epam.xstack.config.TestConfig;
import epam.xstack.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;

@SpringJUnitConfig(TestConfig.class)
public class GymValidatorTest {
    @Autowired
    GymValidator<User> gymValidator;

    @Test
    void testValidate() {
        User correctUser = new User("Goodname", "Goodsurname",
                "goodname.goodsurname", "password", true);

        Set<String> violations = gymValidator.validate(correctUser);

        assertThat(violations).isEmpty();
    }

    @Test
    void testValidateBadUser() {
        User badUser = new User("", null,
                null, "", true);

        Set<String> violations = gymValidator.validate(badUser);

        assertThat(violations).hasSize(4);
        violations.forEach(violation -> {
            assertThat(violation).contains("cannot be empty");
        });
    }
}
