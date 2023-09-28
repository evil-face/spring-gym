package epam.xstack.service;

import epam.xstack.exception.UnauthorizedException;
import epam.xstack.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.naming.AuthenticationException;
import java.util.Optional;

@Service
public final class AuthenticationService {
    private final UserService userService;
    private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);

    @Autowired
    public AuthenticationService(UserService userService) {
        this.userService = userService;
    }

    public boolean authenticate(String txID, String username, String password) {
        Optional<User> user = userService.findByUsername(txID, username);

        if (user.isPresent()) {
            User principal = user.get();

            if (principal.getUsername().equals(username) && principal.getPassword().equals(password)) {
                return true;
            } else {
                throw new UnauthorizedException(txID);
            }
        } else {
            LOGGER.warn("TX ID: {} — No user was found to authenticate with username {}", txID, username);
            throw new UnauthorizedException(txID);
        }
    }

    public boolean updatePassword(String txID, String username, String oldPassword, String newPassword) throws AuthenticationException {
        if (authenticate(txID, username, oldPassword)) {
            return userService.updatePassword(txID, username, newPassword);
        }

        return false;
    }
}
