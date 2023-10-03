package epam.xstack.unit.controller;

import epam.xstack.controller.AuthController;
import epam.xstack.dto.auth.AuthDTO;
import epam.xstack.dto.auth.PasswordChangeRequestDTO;
import epam.xstack.model.User;
import epam.xstack.service.AuthenticationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;

import javax.servlet.http.HttpServletRequest;

import static org.mockito.ArgumentMatchers.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthControllerTest {
    @InjectMocks
    AuthController authController;
    @Mock
    AuthenticationService authService;
    @Mock
    private ModelMapper modelMapper;
    private static final String TX_ID = "12345";
    public static final String USERNAME = "test.test";
    private static final String CORRECT_PASSWORD = "qwerty";

    @Test
    void testLogin_ReturnsOkResponseEntity() {
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        AuthDTO authDTO = new AuthDTO();
        User user = getTestUser();
        BindingResult bindingResult = new BeanPropertyBindingResult(authDTO, "authDTO");

        when(mockRequest.getAttribute("txID")).thenReturn(TX_ID);
        when(modelMapper.map(eq(authDTO), eq(User.class))).thenReturn(user);
        when(authService.authenticate(anyString(), anyLong(), anyString(), anyString())).thenReturn(true);

        ResponseEntity<?> response = authController.handleLogin(1L, authDTO, bindingResult, mockRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(authService).authenticate(anyString(), eq(1L), anyString(), anyString());
        verify(modelMapper).map(eq(authDTO), eq(User.class));
    }

    @Test
    void testLogin_ReturnsUnprocessableEntity() {
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        AuthDTO authDTO = new AuthDTO();
        BindingResult bindingResult = new BeanPropertyBindingResult(authDTO, "authDTO");
        bindingResult.rejectValue("username", "Error message");

        when(mockRequest.getAttribute("txID")).thenReturn(TX_ID);

        ResponseEntity<?> response = authController.handleLogin(1L, authDTO, bindingResult, mockRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(response.getBody().toString()).contains("username").contains("null");

        verifyNoInteractions(authService, modelMapper);
    }

    @Test
    void testLogin_ReturnsUnauthorizedEntity() {
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        AuthDTO authDTO = new AuthDTO();
        User user = getTestUser();
        BindingResult bindingResult = new BeanPropertyBindingResult(authDTO, "authDTO");

        when(mockRequest.getAttribute("txID")).thenReturn(TX_ID);
        when(modelMapper.map(eq(authDTO), eq(User.class))).thenReturn(user);
        when(authService.authenticate(anyString(), anyLong(), anyString(), anyString())).thenReturn(false);

        ResponseEntity<?> response = authController.handleLogin(1L, authDTO, bindingResult, mockRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        verify(authService).authenticate(anyString(), eq(1L), anyString(), anyString());
        verify(modelMapper).map(eq(authDTO), eq(User.class));
    }

    @Test
    void testChangePassword_ReturnsOkResponseEntity() {
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        PasswordChangeRequestDTO requestDTO = new PasswordChangeRequestDTO();
        BindingResult bindingResult = new BeanPropertyBindingResult(requestDTO, "requestDTO");

        when(mockRequest.getAttribute("txID")).thenReturn(TX_ID);
        when(authService.updatePassword(anyString(), anyLong(), any(PasswordChangeRequestDTO.class)))
                .thenReturn(true);

        ResponseEntity<?> response = authController.handleChangePassword(
                1L, requestDTO, bindingResult, mockRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(authService).updatePassword(anyString(), eq(1L), any(PasswordChangeRequestDTO.class));
    }

    @Test
    void testChangePassword_ReturnsUnprocessableEntity() {
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        PasswordChangeRequestDTO requestDTO = new PasswordChangeRequestDTO();
        BindingResult bindingResult = new BeanPropertyBindingResult(requestDTO, "requestDTO");
        bindingResult.rejectValue("newPassword", "Error message");

        when(mockRequest.getAttribute("txID")).thenReturn(TX_ID);

        ResponseEntity<?> response = authController.handleChangePassword(
                1L, requestDTO, bindingResult, mockRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(response.getBody().toString()).contains("newPassword").contains("null");

        verifyNoInteractions(authService, modelMapper);
    }

    @Test
    void testChangePassword_ReturnsNotFoundEntity() {
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        PasswordChangeRequestDTO requestDTO = new PasswordChangeRequestDTO();
        BindingResult bindingResult = new BeanPropertyBindingResult(requestDTO, "requestDTO");

        when(mockRequest.getAttribute("txID")).thenReturn(TX_ID);
        when(authService.updatePassword(anyString(), anyLong(), any(PasswordChangeRequestDTO.class)))
                .thenReturn(false);

        ResponseEntity<?> response = authController.handleChangePassword(
                1L, requestDTO, bindingResult, mockRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        verify(authService).updatePassword(anyString(), eq(1L), any(PasswordChangeRequestDTO.class));
    }

    private User getTestUser() {
        User user = new User();
        user.setUsername(USERNAME);
        user.setPassword(CORRECT_PASSWORD);

        return user;
    }
}
