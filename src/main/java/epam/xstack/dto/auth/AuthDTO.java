package epam.xstack.dto.auth;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.validation.constraints.NotBlank;

public final class AuthDTO {
    @JsonIgnore
    private long id;

    @NotBlank(message = "Username cannot be empty")
    private String username;

    @NotBlank(message = "Password cannot be empty")
    private String password;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
