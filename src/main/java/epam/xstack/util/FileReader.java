package epam.xstack.util;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.TypeResolverBuilder;
import epam.xstack.model.GymEntity;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class FileReader {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    static {
        objectMapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
    }

    private FileReader() {
    }

    public static Map<String, List<GymEntity>> readStorage(String storageFilePath) {
        Map<String, List<GymEntity>> storage = new HashMap<>();

        if (Files.exists(Paths.get(storageFilePath))) {
            File diskStorage = new File(storageFilePath);

            if (diskStorage.length() != 0) {
                try {
                    storage = objectMapper.readValue(
                            diskStorage,
                            new TypeReference<Map<String, List<GymEntity>>>() { } );
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        return storage;
    }

    public static void writeStorage(Map<String, List<GymEntity>> storage, String storageFilePath) {
        if (Files.exists(Paths.get(storageFilePath))) {
            try {
                objectMapper.writeValue(new File(storageFilePath), storage);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
