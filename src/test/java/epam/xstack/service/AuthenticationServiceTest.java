package epam.xstack.service;

import epam.xstack.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {
    @InjectMocks
    AuthenticationService authService;
    @Mock
    UserService userService;

    @Test
    void testAuthenticate() {
        String query = "test.test";
        User mockUser = new User("test", "test", "test.test",
                "12345", true);

        when(userService.findByUsername(query)).thenReturn(Optional.of(mockUser));

        assertThat(authService.authenticate("test.test", "12345")).isTrue();
    }

    @Test
    void testAuthenticateBadCredentials() {
        String query = "test.test";
        User mockUser = new User("test", "test", "test.test",
                "12345", true);

        when(userService.findByUsername(query)).thenReturn(Optional.of(mockUser));

        assertThat(authService.authenticate("test.test", "bad")).isFalse();
    }
}
