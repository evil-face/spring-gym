package epam.xstack.service;

import epam.xstack.dao.UserDAO;
import epam.xstack.service.UserService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.UUID;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    private static final int PASSWORD_LENGTH = 10;
    @InjectMocks
    public UserService userService;
    @Mock
    public UserDAO userDAO;

    @Test
    void testGenerateId() {
        String actual = userService.generateId();

        Assertions.assertDoesNotThrow(() -> UUID.fromString(actual));
    }

    @Test
    void testGenerateUsername() {
        String expected = "john.wick";
        when(userDAO.existsByUsername(expected)).thenReturn(false);
        String actual = userService.generateUsername(" john ", "wi ck ");

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void testGenerateUsernameDuplicate() {
        String expected = "john.wick";
        Mockito.doReturn(true).when(userDAO).existsByUsername(expected);
        String actual = userService.generateUsername(" john ", "wi ck ");

        assertThat(actual).isEqualTo(expected + "1");
    }

    @Test
    void testGenerateUsernameBadInput() {
        String expected = "john.wick";
        Mockito.doReturn(false).when(userDAO).existsByUsername(expected);
        String actual = userService.generateUsername("1john2", "3wick444");

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void generatePassword() {
        String actual = userService.generatePassword();
        assertThat(actual).hasSize(PASSWORD_LENGTH);
    }
}
