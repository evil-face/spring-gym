package epam.xstack.controller;

import epam.xstack.dto.auth.PasswordChangeRequestDTO;
import epam.xstack.dto.auth.AuthDTO;
import epam.xstack.model.User;
import epam.xstack.service.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping(value = "/api/v1/auth", consumes = {"application/JSON"}, produces = {"application/JSON"})
public final class AuthController {
    private final AuthenticationService authService;
    private final ModelMapper modelMapper;
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthController.class);
    private static final String LOG_MESSAGE_WITH_ERRORS = "TX ID: {} — {} — {}";
    private static final String LOG_MESSAGE = "TX ID: {} — {}";

    @Autowired
    public AuthController(AuthenticationService authService, ModelMapper modelMapper) {
        this.authService = authService;
        this.modelMapper = modelMapper;
    }

    @GetMapping("/{id}")
    @Operation(summary = "Authenticate user",
        responses = {
            @ApiResponse(responseCode = "200", description = "User authenticated successfully"),
            @ApiResponse(responseCode = "401", description = "Bad credentials"),
            @ApiResponse(responseCode = "403", description = "Access denied (wrong ID?)"),
            @ApiResponse(responseCode = "422", description = "Username or password is null")})
    public ResponseEntity<?> handleLogin(@PathVariable("id") long id,
                                         @RequestBody @Valid AuthDTO authDTO,
                                         BindingResult bindingResult,
                                         HttpServletRequest httpServletRequest) {
        String txID = (String) httpServletRequest.getAttribute("txID");

        if (bindingResult.hasErrors()) {
            Map<String, String> errors = buildErrorMessage(bindingResult);
            LOGGER.warn(LOG_MESSAGE_WITH_ERRORS, txID, HttpStatus.UNPROCESSABLE_ENTITY, errors);

            return ResponseEntity.unprocessableEntity().body(errors);
        }

        User user = modelMapper.map(authDTO, User.class);

        if (authService.authenticate(txID, id, user.getUsername(), user.getPassword())) {
            LOGGER.info(LOG_MESSAGE, txID, HttpStatus.OK);

            return ResponseEntity.ok().build();
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @PutMapping("/{id}")
    @Operation(summary = "Change user password",
        responses = {
            @ApiResponse(responseCode = "200", description = "User changed password successfully"),
            @ApiResponse(responseCode = "401", description = "Bad credentials"),
            @ApiResponse(responseCode = "403", description = "Access denied (wrong ID?)"),
            @ApiResponse(responseCode = "404", description = "User with ID not found"),
            @ApiResponse(responseCode = "422", description = "Username or password is null")})
    public ResponseEntity<?> handleChangePassword(@PathVariable("id") long id,
                                                  @RequestBody @Valid PasswordChangeRequestDTO requestDTO,
                                                  BindingResult bindingResult,
                                                  HttpServletRequest httpServletRequest) {
        String txID = (String) httpServletRequest.getAttribute("txID");

        if (bindingResult.hasErrors()) {
            Map<String, String> errors = buildErrorMessage(bindingResult);
            LOGGER.warn(LOG_MESSAGE_WITH_ERRORS, txID, HttpStatus.UNPROCESSABLE_ENTITY, errors);

            return ResponseEntity.unprocessableEntity().body(errors);
        }

        boolean success = authService.updatePassword(txID, id, requestDTO);

        if (success) {
            LOGGER.info(LOG_MESSAGE, txID, HttpStatus.OK);
            return ResponseEntity.ok().build();
        } else {
            LOGGER.info(LOG_MESSAGE, txID, HttpStatus.NOT_FOUND);
            return ResponseEntity.notFound().build();
        }
    }

    private Map<String, String> buildErrorMessage(BindingResult bindingResult) {
        Map<String, String> errors = new HashMap<>();

        bindingResult.getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage()));

        return errors;
    }
}
