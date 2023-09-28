package epam.xstack.service;

import epam.xstack.dao.UserDAO;
import epam.xstack.model.User;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;


@Service
public final class UserService {
    private final UserDAO userDAO;
    private static final int PASSWORD_LENGTH = 10;

    @Autowired
    public UserService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    public String generateUsername(String firstName, String lastName) {
        if (firstName != null && lastName != null) {
            String clearedFirstName = clearString(firstName);
            String clearedLastName = clearString(lastName);
            String newUsername = clearedFirstName + "." + clearedLastName;

            List<String> usernames = userDAO.findUsernameOccurencies(newUsername);
            OptionalInt max = usernames.stream()
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
        return userDAO.updatePassword(txID, username, newPassword);
    }

    public Optional<User> findByUsername(String txID, String username) {
        return userDAO.findByUsername(txID, username);
    }

    private static String clearString(String s) {
        return s.trim().toLowerCase()
                .replace(" ", "")
                .replaceAll("\\d", "");
    }
}
