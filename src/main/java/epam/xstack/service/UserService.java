package epam.xstack.service;

import epam.xstack.model.User;
import epam.xstack.repository.UserRepository;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;


@Service
public final class UserService {
    private final UserRepository userRepository;

    private static final int PASSWORD_LENGTH = 10;
    private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
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

    public boolean updatePassword(String txID, String username, String newPassword) {
        Optional<User> userOpt = userRepository.findByUsername(username);

        if (userOpt.isEmpty()) {
            LOGGER.warn("TX ID: {} — No trainees were found for username {}", txID, username);
            return false;
        } else {
            User user = userOpt.get();
            user.setPassword(newPassword);
            userRepository.save(user);

            LOGGER.info("TX ID: {} — Successfully updated password of trainee with username {}", txID, username);
            return true;
        }
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    private static String clearString(String s) {
        return s.trim().toLowerCase()
                .replace(" ", "")
                .replaceAll("\\d", "");
    }
}
