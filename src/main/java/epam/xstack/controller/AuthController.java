package epam.xstack.controller;

import epam.xstack.dto.auth.PasswordChangeRequestDTO;
import epam.xstack.dto.auth.AuthDTO;
import epam.xstack.exception.ValidationException;
import epam.xstack.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/api/v1/auth", consumes = {"application/JSON"}, produces = {"application/JSON"})
public final class AuthController {
    private final UserService userService;
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthController.class);
    private static final String LOG_MESSAGE = "TX ID: {} — {}";

    @Autowired
    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    @Operation(summary = "Authenticate user and get JWT token",
        responses = {
            @ApiResponse(responseCode = "200", description = "User authenticated successfully"),
            @ApiResponse(responseCode = "401", description = "Bad credentials"),
            @ApiResponse(responseCode = "422", description = "Username or password is empty")})
    public ResponseEntity<?> handleLogin(@RequestBody @Valid AuthDTO authDTO,
                                         BindingResult bindingResult,
                                         HttpServletRequest httpServletRequest) {
        String txID = (String) httpServletRequest.getAttribute("txID");
        validatePayload(txID, bindingResult);

        String jwtToken = userService.loginAndGenerateToken(txID, authDTO);

        LOGGER.info(LOG_MESSAGE, txID, HttpStatus.OK);
        return ResponseEntity.ok(Map.of("jwt-token", jwtToken));
    }

    @PutMapping("/{id}/password")
    @Operation(summary = "Change user password",
        responses = {
            @ApiResponse(responseCode = "200", description = "User changed password successfully"),
            @ApiResponse(responseCode = "401", description = "Bad credentials"),
            @ApiResponse(responseCode = "403", description = "Access denied (wrong ID?)"),
            @ApiResponse(responseCode = "404", description = "User with ID not found"),
            @ApiResponse(responseCode = "422", description = "Username or password is null")})
    public ResponseEntity<?> handleChangePassword(@RequestBody @Valid PasswordChangeRequestDTO requestDTO,
                                                  BindingResult bindingResult,
                                                  HttpServletRequest httpServletRequest) {
        String txID = (String) httpServletRequest.getAttribute("txID");
        validatePayload(txID, bindingResult);

        boolean success = userService.updatePassword(txID, requestDTO);

        if (success) {
            LOGGER.info(LOG_MESSAGE, txID, HttpStatus.OK);
            return ResponseEntity.ok().build();
        } else {
            LOGGER.info(LOG_MESSAGE, txID, HttpStatus.NOT_FOUND);
            return ResponseEntity.notFound().build();
        }
    }

    private void validatePayload(String txID, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            List<String> errors = bindingResult.getFieldErrors().stream()
                    .map(error -> error.getField() + " - " + error.getDefaultMessage()).toList();

            throw new ValidationException(txID, errors.toString());
        }
    }
}
