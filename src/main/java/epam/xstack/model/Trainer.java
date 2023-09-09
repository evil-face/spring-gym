package epam.xstack.model;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

public class Trainer extends User {
    public Trainer(String id, String firstName, String lastName, String username, String password, boolean isActive) {
        super(id, firstName, lastName, username, password, isActive);
    }
}
