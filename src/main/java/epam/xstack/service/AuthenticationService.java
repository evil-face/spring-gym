package epam.xstack.service;

import epam.xstack.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public final class AuthenticationService {
    private final UserService userService;

    @Autowired
    public AuthenticationService(UserService userService) {
        this.userService = userService;
    }

    public boolean authenticate(String username, String password) {
        Optional<User> user = userService.findByUsername(username);

        if (user.isPresent()) {
            User principal = user.get();

            return principal.getUsername().equals(username)
                    && principal.getPassword().equals(password);
        }

        return false;
    }
}
