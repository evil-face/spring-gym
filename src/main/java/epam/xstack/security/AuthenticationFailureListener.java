package epam.xstack.security;

import epam.xstack.service.LoginAttemptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

@Component
public final class AuthenticationFailureListener
        implements ApplicationListener<AuthenticationFailureBadCredentialsEvent> {
    @Autowired
    private HttpServletRequest request;

    @Autowired
    private LoginAttemptService loginAttemptService;

    @Override
    public void onApplicationEvent(AuthenticationFailureBadCredentialsEvent e) {
        if (e.getSource() instanceof UsernamePasswordAuthenticationToken) {
            String username = e.getAuthentication().getPrincipal().toString();
            String txID = (String) request.getAttribute("txID");

            loginAttemptService.handleFailedLoginAttempt(txID, username);
        }
    }
}
