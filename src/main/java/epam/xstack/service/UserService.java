package epam.xstack.service;

import epam.xstack.dao.UserDAO;
import epam.xstack.model.User;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;


@Service
public final class UserService {
    private final UserDAO userDAO;
    private static final int PASSWORD_LENGTH = 10;

    @Autowired
    public UserService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    public String generateUsername(String firstName, String lastName) {
        String newUsername = null;

        if(firstName != null && lastName != null) {
            String clearedFirstName = firstName.trim().toLowerCase()
                    .replace(" ", "")
                    .replaceAll("\\d", "");

            String clearedLastName = lastName.trim().toLowerCase()
                    .replace(" ", "")
                    .replaceAll("\\d", "");

            newUsername = clearedFirstName + "." + clearedLastName;

        while (userDAO.existsByUsername(newUsername)) {
            String[] detachedUsername = newUsername.split("(?<=[a-zA-Z])(?=\\d)");
            while (userDAO.existsByUsername(newUsername)) {
                String[] detachedUsername = newUsername.split("(?<=[a-zA-Z])(?=\\d)");

                if (detachedUsername.length == 1) {
                    newUsername = detachedUsername[0] + "1";
                } else {
                    newUsername = detachedUsername[0] + (Integer.parseInt(detachedUsername[1]) + 1);
                }
            }
        }

        return newUsername;
    }

    public String generatePassword() {
        return RandomStringUtils.randomAlphanumeric(PASSWORD_LENGTH);
    }

    public Optional<User> findByUsername(String username) {
        return userDAO.findByUsername(username);
    }
}
