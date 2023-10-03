package epam.xstack.unit.service;

import epam.xstack.exception.ForbiddenException;
import epam.xstack.exception.UnauthorizedException;
import epam.xstack.model.User;
import epam.xstack.service.AuthenticationService;
import epam.xstack.service.UserService;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
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
    private static final String TX_ID = "12345";
    public static final String USERNAME = "test.test";
    private static final String CORRECT_PASSWORD = "qwerty";

    @Test
    void testAuthenticateSuccess() {
        when(userService.findByUsername(TX_ID, USERNAME)).thenReturn(Optional.of(getMockUser()));

        assertThat(authService.authenticate(TX_ID, 1, USERNAME, CORRECT_PASSWORD)).isTrue();
    }

    @Test
    void testAuthenticateNoSuchUser() {
        when(userService.findByUsername(TX_ID, USERNAME)).thenReturn(Optional.empty());

        Assertions.assertThrows(UnauthorizedException.class,
                () -> authService.authenticate(TX_ID, 1, USERNAME, CORRECT_PASSWORD));
    }

    @Test
    void testAuthenticateWrongPassword() {
        when(userService.findByUsername(TX_ID, USERNAME)).thenReturn(Optional.of(getMockUser()));

        Assertions.assertThrows(UnauthorizedException.class,
                () -> authService.authenticate(TX_ID, 1, USERNAME, "wrong_password"));
    }

    @Test
    void testAuthenticateWrongIdAccess() {
        when(userService.findByUsername(TX_ID, USERNAME)).thenReturn(Optional.of(getMockUser()));

        Assertions.assertThrows(ForbiddenException.class,
                () -> authService.authenticate(TX_ID, 100, USERNAME, CORRECT_PASSWORD));
    }

    @NotNull
    private static User getMockUser() {
        User mockUser = new User("test", "test", USERNAME,
                CORRECT_PASSWORD, true);
        mockUser.setId(1);

        return mockUser;
    }

}
