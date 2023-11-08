package epam.xstack.unit.controller;

import epam.xstack.controller.AuthController;
import epam.xstack.dto.auth.AuthDTO;
import epam.xstack.dto.auth.PasswordChangeRequestDTO;
import epam.xstack.exception.UnauthorizedException;
import epam.xstack.exception.ValidationException;

import epam.xstack.service.UserService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;

import javax.servlet.http.HttpServletRequest;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {
    @InjectMocks
    AuthController authController;

    @Mock
    UserService userService;

    private static final String TX_ID = "12345";

    @Test
    void testLogin_ReturnsOkResponseEntity() {
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        AuthDTO authDTO = new AuthDTO();
        BindingResult bindingResult = new BeanPropertyBindingResult(authDTO, "authDTO");
        String expectedJwtToken = "a1b2bc3";

        when(mockRequest.getAttribute("txID")).thenReturn(TX_ID);
        when(userService.loginAndGenerateToken(TX_ID, authDTO)).thenReturn(expectedJwtToken);

        ResponseEntity<?> response = authController.handleLogin(authDTO, bindingResult, mockRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(Map.of("jwt-token", expectedJwtToken));
        verify(userService).loginAndGenerateToken(TX_ID, authDTO);
    }

    @Test
    void testLogin_ReturnsUnprocessableEntity() {
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        AuthDTO authDTO = new AuthDTO();
        BindingResult bindingResult = new BeanPropertyBindingResult(authDTO, "authDTO");
        bindingResult.rejectValue("username", "Error message");

        when(mockRequest.getAttribute("txID")).thenReturn(TX_ID);

        Assertions.assertThrows(ValidationException.class, () -> {
            authController.handleLogin(authDTO, bindingResult, mockRequest);
        });

        verifyNoInteractions(userService);
    }

    @Test
    void testLogin_ReturnsUnauthorizedEntity() {
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        AuthDTO authDTO = new AuthDTO();
        BindingResult bindingResult = new BeanPropertyBindingResult(authDTO, "authDTO");

        when(mockRequest.getAttribute("txID")).thenReturn(TX_ID);
        when(userService.loginAndGenerateToken(TX_ID, authDTO)).thenThrow(new UnauthorizedException(TX_ID));

        Assertions.assertThrows(UnauthorizedException.class, () -> {
            authController.handleLogin(authDTO, bindingResult, mockRequest);
        });

        verify(userService).loginAndGenerateToken(TX_ID, authDTO);
    }

    @Test
    void testChangePassword_ReturnsOkResponseEntity() {
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        PasswordChangeRequestDTO requestDTO = new PasswordChangeRequestDTO();
        BindingResult bindingResult = new BeanPropertyBindingResult(requestDTO, "requestDTO");

        when(mockRequest.getAttribute("txID")).thenReturn(TX_ID);
        when(userService.updatePassword(TX_ID, requestDTO)).thenReturn(true);

        ResponseEntity<?> response = authController.handleChangePassword(requestDTO, bindingResult, mockRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(userService).updatePassword(TX_ID, requestDTO);
    }

    @Test
    void testChangePassword_ReturnsUnprocessableEntity() {
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        PasswordChangeRequestDTO requestDTO = new PasswordChangeRequestDTO();
        BindingResult bindingResult = new BeanPropertyBindingResult(requestDTO, "requestDTO");
        bindingResult.rejectValue("newPassword", "Error message");

        when(mockRequest.getAttribute("txID")).thenReturn(TX_ID);

        Assertions.assertThrows(ValidationException.class, () -> {
            authController.handleChangePassword(requestDTO, bindingResult, mockRequest);
        });

        verifyNoInteractions(userService);
    }

    @Test
    void testChangePassword_ReturnsNotFoundEntity() {
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        PasswordChangeRequestDTO requestDTO = new PasswordChangeRequestDTO();
        BindingResult bindingResult = new BeanPropertyBindingResult(requestDTO, "requestDTO");

        when(mockRequest.getAttribute("txID")).thenReturn(TX_ID);
        when(userService.updatePassword(TX_ID, requestDTO)).thenReturn(false);

        ResponseEntity<?> response = authController.handleChangePassword(requestDTO, bindingResult, mockRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        verify(userService).updatePassword(TX_ID, requestDTO);
    }
}
