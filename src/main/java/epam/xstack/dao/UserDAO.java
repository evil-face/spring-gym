package epam.xstack.dao;

import epam.xstack.repository.MapRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UserDAO {
        private MapRepository mapRepository;

        @Autowired
        public UserDAO(MapRepository mapRepository) {
                this.mapRepository = mapRepository;
        }

        public boolean existsByUsername(String newUsername) {
                return mapRepository.existsByUsername(newUsername);
        }
}
