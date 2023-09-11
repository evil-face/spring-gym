package epam.xstack.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import epam.xstack.model.GymEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class FileReader {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final Logger LOGGER = LoggerFactory.getLogger(FileReader.class);

    static {
        OBJECT_MAPPER.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
    }

    private FileReader() {
    }

    public static Map<String, List<GymEntity>> readStorage(String storageFilePath) {
        Map<String, List<GymEntity>> storage = new HashMap<>();

        if (Files.exists(Paths.get(storageFilePath))) {
            LOGGER.info("Found file " + storageFilePath + ", trying to parse.");
            File diskStorage = new File(storageFilePath);

            if (diskStorage.length() != 0) {
                try {
                    storage = OBJECT_MAPPER.readValue(
                            diskStorage,
                            new TypeReference<Map<String, List<GymEntity>>>() { });
                    LOGGER.info("Successfully parsed storage from disk.");
                } catch (IOException e) {
                    LOGGER.error("Error while parsing file, quitting.");
                    throw new RuntimeException(e);
                }
            }
        }

        return storage;
    }

    public static void writeStorage(Map<String, List<GymEntity>> storage, String storageFilePath) {
        if (Files.exists(Paths.get(storageFilePath))) {
            LOGGER.info("Found file " + storageFilePath + ", trying to write.");
            try {
                OBJECT_MAPPER.writeValue(new File(storageFilePath), storage);
                LOGGER.info("Successfully saved storage to disk.");
            } catch (IOException e) {
                LOGGER.error("Error while writing file, quitting.");
                throw new RuntimeException(e);
            }
        }
    }
}
