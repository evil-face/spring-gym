package epam.xstack.unit.service;

import epam.xstack.model.User;
import epam.xstack.repository.UserRepository;
import epam.xstack.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @InjectMocks
    UserService userService;
    @Mock
    UserRepository userRepository;

    private static final int PASSWORD_LENGTH = 10;

    @Test
    void testGenerateUsername() {
        String expected = "john.wick";
        when(userRepository.findByUsernameStartingWith(expected)).thenReturn(Collections.emptyList());
        String actual = userService.generateUsername(" john ", "wi ck ");

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void testGenerateUsernameDuplicate() {
        String expected = "john.wick";
        User user = new User();
        user.setUsername("john.wick");

        when(userRepository.findByUsernameStartingWith(expected)).thenReturn(List.of(user));
        String actual = userService.generateUsername(" john ", "wi ck ");

        assertThat(actual).isEqualTo(expected + "1");
    }

    @Test
    void testGenerateUsernameBadInput() {
        String expected = "john.wick";
        when(userRepository.findByUsernameStartingWith(expected)).thenReturn(Collections.emptyList());
        String actual = userService.generateUsername("1john2", "3wick444");

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void testGenerateUsernameNullInput() {
        String expected = "";
        String actual = userService.generateUsername(null, null);

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void generatePassword() {
        String actual = userService.generatePassword();
        assertThat(actual).hasSize(PASSWORD_LENGTH);
    }
}
