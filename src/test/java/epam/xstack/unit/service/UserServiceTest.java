package epam.xstack.unit.service;

import epam.xstack.dto.auth.AuthDTO;
import epam.xstack.dto.auth.PasswordChangeRequestDTO;
import epam.xstack.exception.UnauthorizedException;
import epam.xstack.model.User;
import epam.xstack.repository.UserRepository;
import epam.xstack.service.TokenService;
import epam.xstack.service.UserService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @InjectMocks
    UserService userService;

    @Mock
    UserRepository userRepository;

    @Mock
    PasswordEncoder passwordEncoder;

    @Mock
    AuthenticationManager authManager;

    @Mock
    TokenService tokenService;

    private static final int PASSWORD_LENGTH = 10;
    private static final String TX_ID = "12345";


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
    void testGeneratePassword() {
        String actual = userService.generatePassword();
        assertThat(actual).hasSize(PASSWORD_LENGTH);
    }

    @Test
    void testUpdatePassword() {
        String username = "test";
        String oldPassword = "oldpass";
        String newPassword = "newpass";

        PasswordChangeRequestDTO requestDTO = new PasswordChangeRequestDTO();
        requestDTO.setUsername(username);
        requestDTO.setOldPassword(oldPassword);
        requestDTO.setNewPassword(newPassword);

        User user = new User();
        user.setUsername(username);
        user.setPassword(oldPassword);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(oldPassword, oldPassword)).thenReturn(true);
        when(passwordEncoder.encode(newPassword)).thenReturn("encodedpass");
        boolean actual = userService.updatePassword(TX_ID, requestDTO);

        assertThat(actual).isTrue();
        assertThat(user.getPassword()).isEqualTo("encodedpass");
        verify(userRepository).save(user);
    }

    @Test
    void testUpdatePasswordNoSuchUser() {
        String username = "test";
        String newPassword = "newpass";

        PasswordChangeRequestDTO requestDTO = new PasswordChangeRequestDTO();
        requestDTO.setUsername(username);
        requestDTO.setNewPassword(newPassword);

        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());
        boolean actual = userService.updatePassword(TX_ID, requestDTO);

        assertThat(actual).isFalse();
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void testLoginAndGenerateToken() {
        AuthDTO authDTO = new AuthDTO();
        String expected = "jwttoken";

        when(tokenService.generateToken(any())).thenReturn(expected);

        String actual = userService.loginAndGenerateToken(TX_ID, authDTO);

        assertThat(actual).isEqualTo(expected);
        verify(authManager).authenticate(any());
        verify(tokenService).generateToken(any());
    }

    @Test
    void testLoginAndGenerateTokenBadCredentials() {
        AuthDTO authDTO = new AuthDTO();

        when(authManager.authenticate(any())).thenThrow(BadCredentialsException.class);

        Assertions.assertThrows(UnauthorizedException.class,
                () -> userService.loginAndGenerateToken(TX_ID, authDTO));

        verifyNoInteractions(tokenService);
    }
}
