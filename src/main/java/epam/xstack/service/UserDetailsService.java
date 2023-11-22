package epam.xstack.service;

import epam.xstack.exception.UserTemporarilyBlockedException;
import epam.xstack.model.User;
import epam.xstack.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public final class UserDetailsService implements org.springframework.security.core.userdetails.UserDetailsService {
    private final UserRepository userRepository;
    private final LoginAttemptService loginAttemptService;
    private static final Logger LOGGER = LoggerFactory.getLogger(UserDetailsService.class);


    @Autowired
    public UserDetailsService(UserRepository userRepository, LoginAttemptService loginAttemptService) {
        this.userRepository = userRepository;
        this.loginAttemptService = loginAttemptService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        Optional<User> userOpt = userRepository.findByUsername(username);

        if (userOpt.isEmpty()) {
            throw new UsernameNotFoundException("No TX");
        }

        if (loginAttemptService.isUserBlocked(username)) {
            throw new UserTemporarilyBlockedException("No TX");
        }

        return new epam.xstack.security.UserDetails(userOpt.get());
    }
}
