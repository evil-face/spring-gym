package epam.xstack.model;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = Trainee.class, name = "Trainee"),
        @JsonSubTypes.Type(value = Trainer.class, name = "Trainer")
})
public class Trainer extends User {
    public Trainer(long id, String firstName, String lastName, String username, String password, boolean isActive) {
        super(id, firstName, lastName, username, password, isActive);
    }
}
