package epam.xstack.model;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.springframework.lang.NonNull;

import java.util.Objects;
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = Trainee.class, name = "Trainee"),
    @JsonSubTypes.Type(value = Trainer.class, name = "Trainer")
})
public class User implements GymEntity {
    @NonNull
    private String id;
    @NonNull
    private String firstName;
    @NonNull
    private String lastName;
    @NonNull
    private String username;
    @NonNull
    private String password;
    @NonNull
    private boolean isActive;

    public User(@NonNull String id, @NonNull String firstName, @NonNull String lastName,
                @NonNull String username, @NonNull String password, boolean isActive) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
        this.password = password;
        this.isActive = isActive;
    }

    public User() {
    }

    public final String getId() {
        return id;
    }

    public final void setId(String id) {
        this.id = id;
    }

    @NonNull
    public final String getFirstName() {
        return firstName;
    }

    public final void setFirstName(@NonNull String firstName) {
        this.firstName = firstName;
    }

    @NonNull
    public final String getLastName() {
        return lastName;
    }

    public final void setLastName(@NonNull String lastName) {
        this.lastName = lastName;
    }

    @NonNull
    public final String getUsername() {
        return username;
    }

    public final void setUsername(@NonNull String username) {
        this.username = username;
    }

    @NonNull
    public final String getPassword() {
        return password;
    }

    public final void setPassword(@NonNull String password) {
        this.password = password;
    }

    public final boolean isActive() {
        return isActive;
    }

    public final void setActive(boolean active) {
        isActive = active;
    }

    /**
     * Override this in child.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        User user = (User) o;
        return id.equals(user.id)
                && isActive == user.isActive
                && Objects.equals(firstName, user.firstName)
                && Objects.equals(lastName, user.lastName)
                && Objects.equals(username, user.username)
                && Objects.equals(password, user.password);
    }

    /**
     * Override this in child.
     */
    @Override
    public int hashCode() {
        return Objects.hash(id, firstName, lastName, username, password, isActive);
    }
}
