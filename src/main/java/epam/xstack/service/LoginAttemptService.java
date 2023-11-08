package epam.xstack.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;

@Service
public final class LoginAttemptService {
    @Value("${bruteforce.protection.max-attempts}")
    private int maxAttempts;

    @Value("${bruteforce.protection.block-period-minutes}")
    public int blockPeriod;

    @Value("${bruteforce.protection.cache-size}")
    private int maxCacheSize;

    private final ConcurrentHashMap<String, FailedLogin> cache;
    private static final Logger LOGGER = LoggerFactory.getLogger(LoginAttemptService.class);

    public LoginAttemptService() {
        this.cache = new ConcurrentHashMap<>(maxCacheSize);
    }

    public void handleFailedLoginAttempt(String txID, String username) {
        FailedLogin failedLogin = cache.get(username.toLowerCase());

        if (failedLogin == null) {
            failedLogin = new FailedLogin();
            failedLogin.setCount(1);
            cache.put(username.toLowerCase(), failedLogin);

            decideIfClearCache();
            LOGGER.info("TX ID: {} — Unsuccessful login attempt number 1 for username '{}'", txID, username);
        } else {
            failedLogin.setCount(failedLogin.getCount() + 1);
            failedLogin.setDate(LocalDateTime.now());

            LOGGER.info("TX ID: {} — Unsuccessful login attempt number {} for username '{}'",
                    txID, failedLogin.getCount(), username);
        }
    }

    public boolean isUserBlocked(final String username) {
        FailedLogin failedLogin = cache.get(username.toLowerCase());

        if (failedLogin != null) {
            if (failedLogin.getDate().isBefore(LocalDateTime.now().minusMinutes(blockPeriod))) {
                cache.put(username.toLowerCase(), new FailedLogin());
            }
            return failedLogin.getCount() >= maxAttempts;
        } else {
            return false;
        }
    }

    private void decideIfClearCache() {
        if (cache.size() > maxCacheSize) {
            cache.forEach((k, v) -> {
                if (v.getDate().isBefore(LocalDateTime.now().minusDays(1))) {
                    cache.remove(k);
                }
            });
        }
    }

    public static final class FailedLogin {

        private int count;
        private LocalDateTime date;

        public FailedLogin() {
            this.count = 0;
            this.date = LocalDateTime.now();
        }

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }

        public LocalDateTime getDate() {
            return date;
        }

        public void setDate(LocalDateTime date) {
            this.date = date;
        }
    }
}
