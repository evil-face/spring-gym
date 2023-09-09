package epam.xstack.service;

import epam.xstack.dao.UserDAO;
import epam.xstack.model.User;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UserService {
    private final UserDAO userDAO;
    private static final int passwordLength = 10;

    @Autowired
    public UserService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    public String generateId() {
        return UUID.randomUUID().toString();
    }

    public String generateUsername(String firstName, String lastName) {
        String newUsername = firstName.toLowerCase() + "." + lastName.toLowerCase();

        while (userDAO.existsByUsername(newUsername)) {
            String[] detachedUsername = newUsername.split("(?<=[a-zA-Z])(?=[0-9])");

            if (detachedUsername.length == 1) {
                newUsername = detachedUsername[0] + "1";
            } else {
                newUsername = detachedUsername[0] + (Integer.parseInt(detachedUsername[1]) + 1);
            }
        }

        return newUsername;
    }

    public String generatePassword() {
        return RandomStringUtils.randomAlphanumeric(passwordLength);
    }
}
