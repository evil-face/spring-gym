package epam.xstack.service;

import epam.xstack.dto.auth.PasswordChangeRequestDTO;
import epam.xstack.exception.ForbiddenException;
import epam.xstack.exception.UnauthorizedException;
import epam.xstack.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public final class AuthenticationService {
    private final UserService userService;
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticationService.class);

    @Autowired
    public AuthenticationService(UserService userService) {
        this.userService = userService;
    }

    public boolean authenticate(String txID, long id, String username, String password) {
        Optional<User> user = userService.findByUsername(username);

        if (user.isEmpty()) {
            LOGGER.warn("TX ID: {} — No user was found to authenticate with username {}", txID, username);
            throw new UnauthorizedException(txID);
        }

        User principal = user.get();

        if (principal.getPassword().equals(password)) {
            if (principal.getId() == id) {
                return true;
            } else {
                LOGGER.warn("TX ID: {} — Attempt to perform an action with no access with username {}", txID, username);
                throw new ForbiddenException(txID);
            }
        } else {
            LOGGER.warn("TX ID: {} — Bad login attempt with username {}", txID, username);
            throw new UnauthorizedException(txID);
        }

    }

    public boolean updatePassword(String txID, long id, PasswordChangeRequestDTO requestDTO) {
        authenticate(txID, id, requestDTO.getUsername(), requestDTO.getOldPassword());

        return userService.updatePassword(txID, requestDTO.getUsername(), requestDTO.getNewPassword());
    }
}
