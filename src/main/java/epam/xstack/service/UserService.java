package epam.xstack.service;

import epam.xstack.dto.auth.AuthDTO;
import epam.xstack.dto.auth.PasswordChangeRequestDTO;
import epam.xstack.exception.UnauthorizedException;
import epam.xstack.model.User;
import epam.xstack.repository.UserRepository;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;


@Service
public class UserService {
    private final TokenService tokenService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authManager;

    private static final int PASSWORD_LENGTH = 10;
    private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);

    @Autowired
    public UserService(TokenService tokenService, UserRepository userRepository,
                       PasswordEncoder passwordEncoder, AuthenticationManager authManager) {
        this.tokenService = tokenService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authManager = authManager;
    }

    public String generateUsername(String firstName, String lastName) {
        if (firstName != null && lastName != null) {
            String clearedFirstName = clearString(firstName);
            String clearedLastName = clearString(lastName);
            String newUsername = clearedFirstName + "." + clearedLastName;

            List<User> usernames = userRepository.findByUsernameStartingWith(newUsername);
            OptionalInt max = usernames.stream()
                    .map(User::getUsername)
                    .map(s -> s.replace(newUsername, ""))
                    .mapToInt(s -> s.isEmpty() ? 0 : Integer.parseInt(s))
                    .max();

            return max.isEmpty() ? newUsername : newUsername + (max.getAsInt() + 1);
        } else {
            return "";
        }
    }

    public String generatePassword() {
        return RandomStringUtils.randomAlphanumeric(PASSWORD_LENGTH);
    }

    @Transactional
    public boolean updatePassword(String txID, PasswordChangeRequestDTO requestDTO) {
        String username = requestDTO.getUsername();
        String oldPassword = requestDTO.getOldPassword();
        String newPassword = requestDTO.getNewPassword();
        Optional<User> userOpt = userRepository.findByUsername(username);

        if (userOpt.isEmpty()) {
            LOGGER.warn("TX ID: {} — No trainees were found for username {}", txID, username);
            return false;
        } else {
            User user = userOpt.get();

            if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
                LOGGER.warn("TX ID: {} — Authorized user attempted to change password for username '{}' with "
                        + "incorrect password in request", txID, username);
                throw new UnauthorizedException(txID);
            }

            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);

            LOGGER.info("TX ID: {} — Successfully updated password of trainee with username {}", txID, username);
            return true;
        }
    }

    public String loginAndGenerateToken(String txID, AuthDTO authDTO) {
        String username = authDTO.getUsername();
        String password = authDTO.getPassword();

        UsernamePasswordAuthenticationToken authInputToken =
                new UsernamePasswordAuthenticationToken(username, password);

        try {
            Authentication authentication = authManager.authenticate(authInputToken);
            String jwtToken = tokenService.generateToken(authentication);
            LOGGER.info("TX ID: {} — New token granted successfully for username '{}'", txID, username);

            return jwtToken;
        } catch (BadCredentialsException e) {
            throw new UnauthorizedException(txID);
        }
    }

    private static String clearString(String s) {
        return s.trim().toLowerCase()
                .replace(" ", "")
                .replaceAll("\\d", "");
    }
}
