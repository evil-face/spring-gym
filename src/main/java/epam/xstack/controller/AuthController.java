package epam.xstack.controller;

import epam.xstack.dto.auth.PasswordChangeRequestDTO;
import epam.xstack.dto.auth.AuthDTO;
import epam.xstack.model.User;
import epam.xstack.service.AuthenticationService;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.naming.AuthenticationException;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping(value = "/api/v1/auth", consumes = {"application/JSON"}, produces = {"application/JSON"})
public class AuthController {
    private final AuthenticationService authService;
    private final ModelMapper modelMapper;
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    public AuthController(AuthenticationService authService, ModelMapper modelMapper) {
        this.authService = authService;
        this.modelMapper = modelMapper;
    }

    @GetMapping
    public ResponseEntity<?> login(@RequestBody @Valid AuthDTO authDTO, BindingResult bindingResult,
                                   HttpServletRequest httpServletRequest) {
        String txID = (String) httpServletRequest.getAttribute("txID");

        if (bindingResult.hasErrors()) {
            Map<String, String> errors = buildErrorMessage(bindingResult);
            LOGGER.warn("TX ID: {} — " + HttpStatus.UNPROCESSABLE_ENTITY + " — " + errors, txID);

            return ResponseEntity.unprocessableEntity().body(errors);
        }

        User user = modelMapper.map(authDTO, User.class);

        if (authService.authenticate(txID, user.getUsername(), user.getPassword())) {
            LOGGER.info("TX ID: {} — " + HttpStatus.OK, txID);

            return ResponseEntity.ok().build();
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @PutMapping
    public ResponseEntity<?> handleChangePassword(@RequestBody @Valid PasswordChangeRequestDTO request,
                                                  BindingResult bindingResult,
                                                  HttpServletRequest httpServletRequest) throws AuthenticationException {
        String txID = (String) httpServletRequest.getAttribute("txID");

        if (bindingResult.hasErrors()) {
            Map<String, String> errors = buildErrorMessage(bindingResult);
            LOGGER.warn("TX ID: {} — " + HttpStatus.UNPROCESSABLE_ENTITY + " — " + errors, txID);

            return ResponseEntity.unprocessableEntity().body(errors);
        }

        boolean success = authService.updatePassword(txID, request.getUsername(),
                request.getOldPassword(), request.getNewPassword());

        if (success) {
            LOGGER.info("TX ID: {} — " + HttpStatus.OK, txID);
            return ResponseEntity.ok().build();
        } else {
            LOGGER.info("TX ID: {} — " + HttpStatus.UNPROCESSABLE_ENTITY, txID);
            return ResponseEntity.unprocessableEntity().build();
        }

    }

    private Map<String, String> buildErrorMessage(BindingResult bindingResult) {
        Map<String, String> errors = new HashMap<>();

        bindingResult.getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage()));

        return errors;
    }
}
