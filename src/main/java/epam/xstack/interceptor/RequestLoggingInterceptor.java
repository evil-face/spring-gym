package epam.xstack.interceptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

@Component
public class RequestLoggingInterceptor implements HandlerInterceptor {
    private static final Logger LOGGER = LoggerFactory.getLogger(RequestLoggingInterceptor.class);
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String txID = UUID.randomUUID().toString().substring(0, 8);
        request.setAttribute("txID", txID);

        String logMessage = buildRequestLogMessage(txID, request);
        LOGGER.info(logMessage);

        return true;
    }

    private String buildRequestLogMessage(String txID, HttpServletRequest httpServletRequest) {
        String method = httpServletRequest.getMethod();
        String requestURI = httpServletRequest.getRequestURI();
        String protocol = httpServletRequest.getProtocol();

        return "TX ID: %s — %s %s %s".formatted(txID, method, requestURI, protocol);
    }
}
